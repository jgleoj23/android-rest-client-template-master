package com.codepath.apps.restclienttemplate;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * @author Joseph Gardi
 */
public class StackPrinter implements Consumer<Throwable> {
    private String TAG = getClass().getName();
    public static StackPrinter stackPrinter = new StackPrinter();

    @Override
    public void accept(@NonNull Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }
}
