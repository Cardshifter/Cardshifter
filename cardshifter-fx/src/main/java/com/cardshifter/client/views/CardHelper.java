package com.cardshifter.client.views;

import com.cardshifter.api.outgoing.CardInfoMessage;

import java.util.Map;

/**
 * Created by Simon on 2/12/2015.
 */
public class CardHelper {

    public static String stringResources(CardInfoMessage cardInfo) {
        StringBuilder str = new StringBuilder();
        Map<String, Object> props = cardInfo.getProperties();
        if (Integer.valueOf(0).equals(props.get("SICKNESS"))) {
            str.append(" Rush");
        }
        if (Integer.valueOf(1).equals(props.get("DENY_COUNTERATTACK"))) {
            str.append(" Ranged");
        }
        return str.toString();
    }

}
