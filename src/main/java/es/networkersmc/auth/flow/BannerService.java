package es.networkersmc.auth.flow;

import com.google.common.base.Preconditions;
import es.networkersmc.auth.session.AuthSession;
import es.networkersmc.dendera.language.Language;
import es.networkersmc.dendera.minecraft.documentation.Sync;
import es.networkersmc.dendera.module.Module;
import es.networkersmc.dendera.util.bukkit.map.ImageBanner;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BannerService implements Module {

    private final File BANNERS_DIRECTORY = new File("banners");

    @Inject private Server server;
    @Getter private ArmorStand lock;
    private ImageBanner banner;

    @Override
    public void onStart() {
        World world = server.getWorlds().get(0);

        // Already calculated for all FOVs and aspect ratios
        Location spawnLocation = new Location(world, 24.0, 113.0, 7.0, 0, 180);
        banner = new ImageBanner(world, 16 * 3, 8 * 3);
        banner.place(world.getBlockAt(0, 100, 0), BlockFace.SOUTH);

        lock = world.spawn(spawnLocation, ArmorStand.class);
        lock.setVisible(false);
        lock.setGravity(false);
    }

    @Sync
    public void setup(Player player, AuthSession session) {
        Language language = session.getUser().getLanguage();

        BannerImageName bannerImageName;
        switch (session.getState()) {
            case LOGIN: bannerImageName = BannerImageName.LOGIN; break;
            case REGISTER: bannerImageName = BannerImageName.REGISTER; break;
            case CHANGE_PASSWORD: bannerImageName = BannerImageName.CHANGE_PASSWORD; break;
            default:
                // This should never happen
                player.kickPlayer("There was an error. Please contact with us."); // TODO: ERROR MESSAGE (Dendera)
                throw new IllegalStateException("Session state on join wasn't expected: " + session.getState());
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(lock);
        this.updateBanner(player, language, bannerImageName);
    }

    @Sync
    public void displayLoggedIn(Player player) {
        this.playSound(player, Sound.ORB_PICKUP);
    }

    @Sync
    public void displayWrongPassword(Player player, AuthSession session) {
        Language language = session.getUser().getLanguage();

        this.playSound(player, Sound.NOTE_BASS);
        this.updateBanner(player, language, BannerImageName.LOGIN_WRONG_PASSWORD);
    }

    @Sync
    public void displayConfirmPassword(Player player, AuthSession session) {
        Language language = session.getUser().getLanguage();

        this.playSound(player, Sound.ORB_PICKUP);
        this.updateBanner(player, language, BannerImageName.CONFIRM_PASSWORD);
    }

    @Sync
    public void displayPasswordsDontMatch(Player player, AuthSession session, boolean isChangingPassword) {
        Language language = session.getUser().getLanguage();
        BannerImageName bannerImageName = isChangingPassword
                ? BannerImageName.CHANGE_PASSWORD_PASSWORDS_DONT_MATCH
                : BannerImageName.REGISTER_PASSWORDS_DONT_MATCH;

        this.playSound(player, Sound.NOTE_BASS);
        this.updateBanner(player, language, bannerImageName);
    }

    private void updateBanner(Player player, Language language, BannerImageName bannerImageName) {
        File bannerFile = new File(BANNERS_DIRECTORY, language.getCode() + "-" + bannerImageName.toString());

        if (!bannerFile.exists()) {
            Language fallback = Language.getFallback(language);
            Preconditions.checkArgument(fallback != null); // This should never happen as all languages redirect to en_US, and there should be a file for it
            this.updateBanner(player, fallback, bannerImageName);
            return;
        }

        try {
            BufferedImage image = ImageIO.read(bannerFile);
            this.banner.draw(player, image);
        } catch (IOException e) {
            e.printStackTrace();
            player.kickPlayer("There was an error. Please try again."); // TODO: ERROR MESSAGE (Dendera)
        }
    }

    private void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1, 0);
    }
}
