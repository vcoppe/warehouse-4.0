package util;

import agent.Mobile;

public class Condition {

    public static Condition emptyCondition = new Condition();

    public boolean satisfied() {
        return true;
    }

    public boolean satisfied(Mobile mobile) {
        return true;
    }

}
