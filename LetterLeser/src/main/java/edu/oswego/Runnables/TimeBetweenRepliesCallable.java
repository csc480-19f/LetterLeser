package edu.oswego.Runnables;

import edu.oswego.model.Email;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TimeBetweenRepliesCallable implements Callable {
    private static ArrayList<Email> emails;

    public TimeBetweenRepliesCallable(ArrayList<Email> emails){
        this.emails = emails;
    }
    @Override
    public Object call() throws Exception {
        return null;
    }
}