package com.lucidplugins.lucidspices;

import com.google.inject.Provides;
import com.lucidplugins.api.util.*;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.InventoryItemQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.swing.*;
import java.util.Random;

@Extension
@PluginDescriptor(
        name = "Lucid Spices",
        description = "A plugin to help you gather spices for stews and not kill your cat in the process",
        tags = {"lucid", "spice", "spices", "cat", "hellrat"})
public class LucidSpicesPlugin extends Plugin
{

    @Inject
    private LucidSpicesConfig config;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private PluginManager pluginManager;

    private int lastHeal = 0;

    private int lastInteract = 0;

    private boolean shutdown = false;

    private Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private Random rand = new Random();

    @Override
    public void startUp()
    {
        log.info(getName() + " Started");
        shutdown = false;
    }

    @Override
    public void shutDown()
    {
        log.info(getName() + " Stopped");
        shutdown = true;
    }

    @Provides
    private LucidSpicesConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LucidSpicesConfig.class);
    }

    @Subscribe
    private void onGameTick(GameTick tick)
    {
        if (shutdown)
        {
            SwingUtilities.invokeLater( () ->
            {
                try
                {
                    pluginManager.setPluginEnabled(this, false);
                    pluginManager.stopPlugin(this);
                }
                catch (PluginInstantiationException ex)
                {
                    log.error("error stopping plugin", ex);
                }
            });
        }

        runAwayIfLowHealth();

        handleHealing();

        if (config.fullAuto())
        {
            handleEnteringFight();
        }
    }

    private void handleHealing()
    {
        if (ticksSinceLastHeal() < nextInt(4, 5))
        {
            return;
        }

        Item food = getFood();
        NPC cat = getCat();
        WallObject curtain = getCurtain();

        if (food == null || cat == null || curtain == null)
        {
            return;
        }

        int hpPercentage = (int) Math.floor((double) cat.getHealthRatio()  / (double) cat.getHealthScale() * 100);

        if (hpPercentage > config.healPercent())
        {
            return;
        }

        InteractionUtils.useItemOnWallObject(client, food, curtain);

        lastHeal = client.getTickCount();
    }

    private void runAwayIfLowHealth()
    {
        final NPC cat = getCat();

        if (cat == null)
        {
            return;
        }

        final Item food = getFood();

        if (food != null)
        {
            return;
        }

        if (client.getLocalPlayer().getWorldLocation().getX() < 3100)
        {
            return;
        }

        shutdown = true;

        switch (client.getLocalPlayer().getOrientation())
        {
            case 1536:
                InteractionUtils.walk(client.getLocalPlayer().getWorldLocation().dx(-3));
                break;
            case 1024:
                InteractionUtils.walk(client.getLocalPlayer().getWorldLocation().dy(-3));
                break;
            case 512:
                InteractionUtils.walk(client.getLocalPlayer().getWorldLocation().dx(3));
                break;
            case 0:
                InteractionUtils.walk(client.getLocalPlayer().getWorldLocation().dy(3));
                break;
        }

    }

    private void selectDialogOption()
    {
        if (ticksSinceLastInteract() < nextInt(3, 5))
        {
            return;
        }

        if (!DialogUtils.hasOption(client, "Don't insert your cat."))
        {
            return;
        }

        KeyboardUtils.type(client, 1);

        lastInteract = client.getTickCount();
    }

    private void handleEnteringFight()
    {
        NPC cat = getCat();
        if (cat == null)
        {
            return;
        }

        if (!DialogUtils.getOptions(client).isEmpty())
        {
            selectDialogOption();
        }
        else if (canContinue(client))
        {
            KeyboardUtils.sendSpace(client);
        }
        else
        {
            enterFight();
        }
    }

    private void enterFight()
    {
        if (client.getLocalPlayer().getWorldLocation().getX() > 3100 || ticksSinceLastInteract() < nextInt(15, 17) || InventoryUtils.getFreeSlots() == 0)
        {
            return;
        }

        WallObject curtain = getCurtain();

        if (curtain == null)
        {
            return;
        }

        curtain.interact("Enter");

        lastInteract = client.getTickCount();
    }

    private boolean canContinue(Client client)
    {
        final Widget continueWidget = client.getWidget(229, 2);
        if (continueWidget != null && continueWidget.isVisible())
        {
            return true;
        }

        return false;
    }

    private NPC getCat()
    {
        return new NPCQuery().filter(it -> it.getName().toLowerCase().contains("cat") && it.hasAction("Pick-up") ).result(client).nearestTo(client.getLocalPlayer());
    }

    private WallObject getCurtain()
    {
        return new WallObjectQuery().nameEquals("Curtain").result(client).nearestTo(client.getLocalPlayer());
    }

    private Item getFood()
    {
        return new InventoryItemQuery(InventoryID.INVENTORY).idEquals(config.foodId()).result(client).first();
    }

    private int ticksSinceLastHeal()
    {
        return client.getTickCount() - lastHeal;
    }

    private int ticksSinceLastInteract()
    {
        return client.getTickCount() - lastInteract;
    }

    private int nextInt(int min, int max)
    {
        return rand.nextInt((max - min) + 1) + min;
    }
}
