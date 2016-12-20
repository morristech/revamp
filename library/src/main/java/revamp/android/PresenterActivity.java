package revamp.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import revamp.android.delegates.PresenterActivityDelegate;
import revamp.android.delegates.PresenterActivityDelegateCallback;
import revamp.store.RetainableStore;
import revamp.base.Presenter;
import revamp.base.ViewComponent;

public abstract class PresenterActivity<P extends Presenter<V>, V extends ViewComponent> extends Activity implements ViewComponent, PresenterActivityDelegateCallback<V, P>, RetainableStore {

  protected P mPresenter;
  protected PresenterActivityDelegate<V, P> mDelegate;

  @Override
  @CallSuper
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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

  @Override
  public Object onRetainNonConfigurationInstance() {
    return getPresenterDelegate().onRetainCustomNonConfigurationInstance();
  }

  @Override
  public void retainObject(String objectId, Object object) {
    getPresenterDelegate().retainObject(objectId, object);
  }

  @Override
  public Object restoreRetained(String objectId) {
    return getPresenterDelegate().restoreRetained(objectId);
  }

  @Override
  public boolean shouldRetain() {
    return true;
  }

  private PresenterActivityDelegate<V, P> getPresenterDelegate() {
    if (mDelegate == null) {
      mDelegate = new PresenterActivityDelegate<>(this, getLastNonConfigurationInstance());
    }
    return mDelegate;
  }

  @NonNull
  @Override
  public V viewComponent() {
    return (V) this;
  }

  @NonNull
  public P presenter() {
    if (mPresenter == null) {
      mPresenter = buildPresenter();
    }
    return mPresenter;
  }

  @Override
  public void setRetainedPresenter(@NonNull P presenter) {
    mPresenter = presenter;
  }

  public abstract P buildPresenter();
}
