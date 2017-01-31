package com.simprints.id.tools;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPref {
    private SharedPreferences sharedPref;

    private static final String preference_file_key = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a";
    private static final String nudge_mode_bool = "NudgeModeBool";
    private static final String consent_bool = "ConsentBool";
    private static final String quality_threshold = "QualityThresholdInt";
    private static final String nb_of_ids = "NbOfIdsInt";
    private static final String language = "SelectedLanguage";
    private static final String language_position = "SelectedLanguagePosition";
    private static final String db_version_int = "DbVersionInt";
    private static final String matcher_type = "MatcherType";
    private static final String timeout_int = "TimeoutInt";
    private static final String app_key = "AppKey";

    public SharedPref(Context context) {
        sharedPref = context.getSharedPreferences(preference_file_key,
                Context.MODE_PRIVATE);
    }

    /**
     * Nudge mode. This determines if the UI should automatically slide forward
     */
    public Boolean getNudgeModeBool() {
        return sharedPref.getBoolean(nudge_mode_bool, true);
    }

    public void setNudgeModeBool(Boolean nudgeModeBool) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(nudge_mode_bool, nudgeModeBool);
        editor.apply();
    }

    /**
     * Consent. Has the CHW given consent to use Simprints ID
     */
    public Boolean getConsentBool() {
        return sharedPref.getBoolean(consent_bool, true);
    }

    public void setConsentBool(Boolean consentBool) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(consent_bool, consentBool);
        editor.apply();
    }

    /**
     * Quality threshold. This determines the UI feedback for the fingerprint quality
     */
    public int getQualityThresholdInt() {
        return sharedPref.getInt(quality_threshold, 60);
    }

    public void setQualityThresholdInt(int qualityThreshold) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(quality_threshold, qualityThreshold);
        editor.apply();
    }

    /**
     * The number of UIDs to be returned to the calling app
     */
    public int getReturnIdCountInt() {
        return sharedPref.getInt(nb_of_ids, 10);
    }

    public void setReturnIdCountInt(int returnIdCount) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(nb_of_ids, returnIdCount);
        editor.apply();
    }

    /**
     * The selected language
     */
    public String getLanguageString() {
        return sharedPref.getString(language, "");
    }

    public void setLanguageString(String languageString) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(language, languageString);
        editor.apply();
    }

    /**
     * The active language position to be displayed in the list
     */
    public int getLanguagePositionInt() {
        return sharedPref.getInt(language_position, 0);
    }

    public void setLanguagePositionInt(int languagePositionInt) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(language_position, languagePositionInt);
        editor.apply();
    }

    /**
     * Matcher type
     */
    public int getMatcherTypeInt() {
        return sharedPref.getInt(matcher_type, 0);
    }

    public void setMatcherTypeInt(int matcherTypeInt) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(matcher_type, matcherTypeInt);
        editor.apply();
    }

    /**
     * Timeout seconds
     */
    public int getTimeoutInt() {
        return sharedPref.getInt(timeout_int, 3);
    }

    public void setTimeoutInt(int timeoutInt) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(timeout_int, timeoutInt);
        editor.apply();
    }

    /**
     * App Key
     */
    public String getAppKeyString() {
        return sharedPref.getString(app_key, "");
    }

    public void setAppKeyString(String appKey) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(app_key, appKey);
        editor.apply();
    }

}