package observer;

import java.util.LinkedList;

public class Observable {

    private final LinkedList<Observer<Observable>> observers;

    public Observable() {
        this.observers = new LinkedList<>();
    }

    public void attach(Observer<Observable> observer) {
        this.observers.add(observer);
    }

    protected void changed() {
        for (Observer<Observable> observer : this.observers) {
            observer.update(this);
        }
    }

}
