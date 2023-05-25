package org.ishoot.dynaflow.Utils;

import org.ishoot.dynaflow.MainActivity;

public class HumanAwareManager {

    private boolean active;
    private CountDownNoInteraction countDownNoInteraction;

    public HumanAwareManager(MainActivity ma, boolean active) {
        this.active = active;

        if (this.active) {
            ma.humanAwareness = ma.getQiContext().getHumanAwareness();
            ma.humanAwareness.async().addOnEngagedHumanChangedListener(engagedHuman -> {
//                if (getFragment() instanceof SplashFragment) {
//                    if (engagedHuman != null) {
//                        setFragment(new MainFragment());
//                    }
                if (!ma.currentFragment.equals("SplashFragment")) {
                    ma.runOnUiThread(() -> ma.nextFragment("home"));
                } else {
                    reset();
                }
            });
        }
    }

    public void start() {
        if (this.active) countDownNoInteraction.start();
    }

    public void stop() {
        if (this.active) countDownNoInteraction.cancel();
    }

    public void reset() {
        if (this.active) countDownNoInteraction.reset();
    }

}
