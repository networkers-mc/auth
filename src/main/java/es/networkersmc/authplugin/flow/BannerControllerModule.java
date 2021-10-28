package es.networkersmc.authplugin.flow;

import com.google.common.base.Preconditions;
import es.networkersmc.authplugin.event.LoginFailureEvent;
import es.networkersmc.authplugin.event.LoginSuccessEvent;
import es.networkersmc.authplugin.event.PasswordInputErrorEvent;
import es.networkersmc.authplugin.event.PasswordInputEvent;
import es.networkersmc.authplugin.session.AuthSession;
import es.networkersmc.authplugin.session.AuthSessionService;
import es.networkersmc.dendera.bukkit.event.player.UserJoinEvent;
import es.networkersmc.dendera.event.EventService;
import es.networkersmc.dendera.language.Language;
import es.networkersmc.dendera.module.Module;
import es.networkersmc.dendera.network.SwitchboardClient;
import es.networkersmc.dendera.network.bukkit.BukkitNodeType;
import es.networkersmc.dendera.network.packet.request.ConnectUserToBestNodePacket;
import es.networkersmc.dendera.util.bukkit.map.ImageBanner;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BannerControllerModule implements Module, Listener {

    @Inject private @Named("banners-directory") File bannersDirectory;

    @Inject private EventService eventService;
    @Inject private SwitchboardClient switchboardClient;

    @Inject private Server server;
    @Inject private AuthSessionService authSessionService;

    private ImageBanner banner;
    private ArmorStand lock;

    @Override
    public void onStart() {
        this.setupBanner();

        eventService.registerListener(UserJoinEvent.class, event -> {
            Player player = event.getPlayer();
            AuthSession session = authSessionService.getSession(player.getUniqueId());
            Language language = session.getUser().getLanguage();

            BannerImage bannerImage;
            switch (session.getState()) {
                case LOGIN:
                    bannerImage = BannerImage.LOGIN;
                    break;
                case REGISTER:
                    bannerImage = BannerImage.REGISTER;
                    break;
                case CHANGE_PASSWORD:
                    bannerImage = BannerImage.CHANGE_PASSWORD;
                    break;
                default: // This should never happen
                    player.kickPlayer("There was an error. Please contact with us."); // TODO: ERROR MESSAGE (Dendera)
                    throw new IllegalStateException("Session state on join wasn't expected: " + session.getState());
            }

            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(lock);
            this.updateBanner(player, language, bannerImage);
        });

        eventService.registerListener(LoginSuccessEvent.class, event -> {
            this.playSound(event.getPlayer(), Sound.ORB_PICKUP, 0);
            // TODO: Maybe "loading" banner?
            switchboardClient.sendPacket(new ConnectUserToBestNodePacket(event.getPlayer().getUniqueId(), BukkitNodeType.HUB));
        });

        eventService.registerListener(LoginFailureEvent.class, event -> {
            Language language = event.getSession().getUser().getLanguage();

            this.playSound(event.getPlayer(), Sound.NOTE_BASS, 0);
            this.updateBanner(event.getPlayer(), language, BannerImage.LOGIN_WRONG_PASSWORD);
        });

        eventService.registerListener(PasswordInputEvent.class, event -> {
            Language language = event.getSession().getUser().getLanguage();

            this.playSound(event.getPlayer(), Sound.ORB_PICKUP, 0);
            this.updateBanner(event.getPlayer(), language, BannerImage.CONFIRM_PASSWORD);
        });

        eventService.registerListener(PasswordInputErrorEvent.class, event -> {
            Language language = event.getSession().getUser().getLanguage();
            BannerImage bannerImage = event.isChangingPassword()
                    ? BannerImage.CHANGE_PASSWORD_PASSWORDS_DONT_MATCH
                    : BannerImage.REGISTER_PASSWORDS_DONT_MATCH;

            this.playSound(event.getPlayer(), Sound.NOTE_BASS, 0);
            this.updateBanner(event.getPlayer(), language, bannerImage);
        });
    }

    private void setupBanner() {
        World world = server.getWorlds().get(0);

        // Already calculated for all FOVs and aspect ratios
        Location spawnLocation = new Location(world, 24.0, 113.0, 7.0, 0, 180);
        banner = new ImageBanner(world, 16 * 3, 8 * 3);
        banner.place(world.getBlockAt(0, 100, 0), BlockFace.SOUTH);

        lock = world.spawn(spawnLocation, ArmorStand.class);
        lock.setVisible(false);
        lock.setGravity(false);
    }

    private void playSound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 1, pitch);
    }

    private void updateBanner(Player player, Language language, BannerImage bannerImage) {
        File bannerFile = new File(bannersDirectory, language.getCode() + "-" + bannerImage.toString());

        if (!bannerFile.exists()) {
            Language fallback = Language.getFallback(language);
            Preconditions.checkArgument(fallback != null); // This should never happen as all languages redirect to en_US, and there should be a file for it
            this.updateBanner(player, fallback, bannerImage);
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

    @EventHandler
    public void onLeaveLock(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
            event.getPlayer().setSpectatorTarget(lock);
        }
    }
}
