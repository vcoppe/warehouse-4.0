package observer;

interface Observer<T extends Observable> {

    void update(T t);

}
