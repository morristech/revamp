package revamp.android;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.CallSuper;

import revamp.android.delegates.PresenterActivityDelegate;
import revamp.android.delegates.PresenterDelegateCallback;
import revamp.base.Presenter;
import revamp.base.PresenterFactory;
import revamp.base.ViewComponent;

/**
 * Created by mrm on 27/5/15.
 */
public abstract class PresenterActivity<P extends Presenter<V>, V extends ViewComponent> extends Activity implements ViewComponent, PresenterDelegateCallback<V>, PresenterFactory<P>, LoaderManager.LoaderCallbacks<P> {

    private static final int LOADER_ID = 1337;
    private P presenter;
    private PresenterActivityDelegate<V> delegate;

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        getPresenterDelegate().onCreate(savedInstanceState);
    }

    @Override
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        getPresenterDelegate().onDestroy();
    }

    @Override
    @CallSuper
    public void onStart() {
        super.onStart();
        getPresenterDelegate().onStart();
    }

    @Override
    @CallSuper
    public void onStop() {
        super.onStop();
        getPresenterDelegate().onStop();
    }

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
        getPresenterDelegate().onResume();
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        getPresenterDelegate().onPause();
    }

    @Override
    @CallSuper
    public void onRestart() {
        super.onRestart();
        getPresenterDelegate().onRestart();
    }

    @Override
    @CallSuper
    public void onContentChanged() {
        super.onContentChanged();
        getPresenterDelegate().onContentChanged();
    }

    @Override
    @CallSuper
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenterDelegate().onSaveInstanceState(outState);
    }

    @Override
    @CallSuper
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getPresenterDelegate().onPostCreate(savedInstanceState);
    }

    private PresenterActivityDelegate<V> getPresenterDelegate() {
        if (delegate == null) {
            delegate = new PresenterActivityDelegate<>(this);
        }
        return delegate;
    }

    @Override
    public V viewComponent() {
        return (V) this;
    }

    public P presenter() {
        return presenter;
    }

    @Override
    public abstract P buildPresenter();

    @Override
    public Loader<P> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(this, this);
    }

    @Override
    public void onLoadFinished(Loader<P> loader, P data) {
        this.presenter = data;
    }

    @Override
    public void onLoaderReset(Loader<P> loader) {
        presenter = null;
    }
}
