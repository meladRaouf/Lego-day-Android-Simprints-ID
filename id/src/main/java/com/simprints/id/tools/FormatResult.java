package com.simprints.id.tools;

import android.content.Intent;

import com.simprints.id.data.DataManager;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Registration;
import com.simprints.libsimprints.Verification;

import java.util.ArrayList;

public class FormatResult {
    final static private String guidKey = "guid";
    final static private String confidenceKey = "confidence";
    final static private String tierKey = "tier";

    private interface attributeGetter {
        // called this apply so it has a similar signature to function in Java 8
        String apply(Identification identification);
    }

    static private String constructString(ArrayList<Identification> identifications, attributeGetter function) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < identifications.size(); i++) {
            Identification id = identifications.get(i);
            sb.append(function.apply(id));
            if (i < identifications.size() - 1) {
                sb.append(Constants.SIMPRINTS_ODK_RESULT_FORMAT_V01_SEPARATOR);
            }
        }

        return sb.toString();
    }

    static private boolean isODK(String resultFormat) {
        return Constants.SIMPRINTS_ODK_RESULT_FORMAT_V01.equalsIgnoreCase(resultFormat);
    }

    static public void put(Intent intent, Registration registration, String resultFormat) {
        if (isODK(resultFormat)) {
            intent.putExtra(guidKey, registration.getGuid());
        } else {
            intent.putExtra(Constants.SIMPRINTS_REGISTRATION, registration);
        }
    }

    static public void put(Intent intent, Verification verification, String resultFormat) {
        if (isODK(resultFormat)) {
            intent.putExtra(guidKey, verification.getGuid());
            intent.putExtra(confidenceKey, Float.toString(verification.getConfidence()));
            intent.putExtra(tierKey, verification.getTier().toString());
        } else {
            intent.putExtra(Constants.SIMPRINTS_VERIFICATION, verification);
        }
    }

    static public void put(Intent intent, ArrayList<Identification> identifications, DataManager dataManager) {

        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, dataManager.getSessionId());

        if (isODK(dataManager.getResultFormat())) {
            // a bit inefficient to run through the array x times but there will be <= 20 objects in there
            String guids = constructString(identifications, new attributeGetter() {
                @Override
                public String apply(Identification identification) {
                    return identification.getGuid();
                }
            });

            String confidences = constructString(identifications, new attributeGetter() {
                @Override
                public String apply(Identification identification) {
                    return Float.toString(identification.getConfidence());
                }
            });

            String tiers = constructString(identifications, new attributeGetter() {
                @Override
                public String apply(Identification identification) {
                    return identification.getTier().toString();
                }
            });

            intent.putExtra(guidKey, guids);
            intent.putExtra(confidenceKey, confidences);
            intent.putExtra(tierKey, tiers);
        } else {
            intent.putExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications);
        }
    }
}