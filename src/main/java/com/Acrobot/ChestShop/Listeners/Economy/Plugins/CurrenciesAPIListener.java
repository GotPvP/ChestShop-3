package com.Acrobot.ChestShop.Listeners.Economy.Plugins;

import com.Acrobot.ChestShop.Events.Economy.AccountCheckEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAmountEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyCheckEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyFormatEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyHoldEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyTransferEvent;
import com.Acrobot.ChestShop.Listeners.Economy.EconomyAdapter;
import lynn.lace.currenciesapi.api.CurrenciesAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Represents a Reserve connector
 *
 * @author creatorfromhell
 */
public class CurrenciesAPIListener extends EconomyAdapter {

    private DecimalFormat DF = new DecimalFormat("###,###,###,###,###,###,##0.##");

    public static CurrenciesAPI getProvider() {
        return CurrenciesAPI.getInstance();
    }

    public boolean provided() {
        return CurrenciesAPI.getInstance() != null;
    }

    public boolean transactionCanFail() {
        if (CurrenciesAPI.getInstance() == null) {
            return false;
        }

        return false;
    }

    public static @Nullable CurrenciesAPIListener prepareListener() {
        if (Bukkit.getPluginManager().getPlugin("CurrenciesAPI") == null) {
            return null;
        }
        return new CurrenciesAPIListener();
    }

    @EventHandler
    public void onAmountCheck(CurrencyAmountEvent event) {
        if (!provided() || event.wasHandled() || !event.getAmount().equals(BigDecimal.ZERO)) {
            return;
        }

        event.setAmount(CurrenciesAPI.getInstance().getSync("money", Bukkit.getOfflinePlayer(event.getAccount())));
        event.setHandled(true);
    }

    @EventHandler
    public void onCurrencyCheck(CurrencyCheckEvent event) {
        if (!provided() || event.wasHandled() || event.hasEnough()) {
            return;
        }

        event.hasEnough(CurrenciesAPI.getInstance().hasSync("money", Bukkit.getOfflinePlayer(event.getAccount()), event.getAmount().doubleValue()));
        event.setHandled(true);
    }

    @EventHandler
    public void onAccountCheck(AccountCheckEvent event) {
        if (!provided() || event.wasHandled() || event.hasAccount()) {
            return;
        }
        event.hasAccount(Bukkit.getOfflinePlayer(event.getAccount()).hasPlayedBefore());
        event.setHandled(true);
    }

    @EventHandler
    public void onCurrencyFormat(CurrencyFormatEvent event) {
        if ( event.wasHandled() || !event.getFormattedAmount().isEmpty()) {
            return;
        }

        if (provided()) {
            event.setFormattedAmount(DF.format(event.getAmount()));
            event.setHandled(true);
        }
    }

    @EventHandler
    public void onCurrencyAdd(CurrencyAddEvent event) {
        if (!provided() || event.wasHandled()) {
            return;
        }
        CurrenciesAPI.getInstance().give("money", Bukkit.getOfflinePlayer(event.getTarget()), event.getAmount().doubleValue(), "Chest Shop");
        event.setHandled(true);
    }

    @EventHandler
    public void onCurrencySubtraction(CurrencySubtractEvent event) {
        if (!provided() || event.wasHandled()) {
            return;
        }
        CurrenciesAPI.getInstance().take("money", Bukkit.getOfflinePlayer(event.getTarget()), event.getAmount().doubleValue(), "Chest Shop");
        event.setHandled(true);
    }

    @EventHandler
    public void onCurrencyTransfer(CurrencyTransferEvent event) {
        processTransfer(event);
    }

    @EventHandler
    public void onCurrencyHoldCheck(CurrencyHoldEvent event) {
        if (event.getAccount() == null || event.wasHandled() || !transactionCanFail() || event.canHold()) {
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getAccount());
        if (!offlinePlayer.hasPlayedBefore()) {
            event.canHold(false);
            return;
        }

        event.canHold(true);
        event.setHandled(true);
    }
}
