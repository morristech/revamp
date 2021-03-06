package revamp.android.delegates;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.SparseArrayCompat;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import revamp.base.Presenter;
import revamp.base.ViewComponent;
import revamp.store.RetainableStore;
import revamp.store.RetainableStoreImpl;

public class PresenterFragmentDelegate<V extends ViewComponent, P extends Presenter<V>> implements RetainableStore {
  @VisibleForTesting static final String FRAGMENT_TAG = "revamp_fragment";
  @VisibleForTesting static final String STORE_ID = "revamp_store_id";
  private PresenterFragmentDelegateCallback<V, P> mCallback;
  private WeakReference<Activity> mActivityRef;
  private int mStoreId;

  public PresenterFragmentDelegate(@NonNull PresenterFragmentDelegateCallback<V, P> callback, @NonNull Activity activity) {
    mCallback = callback;
    mActivityRef = new WeakReference<>(activity);
  }

  public void onCreate(Bundle savedInstanceState) {
    P presenter;
    if (!mCallback.shouldRetain()) {
      presenter = mCallback.presenter();
    } else if (savedInstanceState != null && savedInstanceState.containsKey(STORE_ID)) {
      // If there is a stored presenter, we use it and let the callback know we are doing it
      mStoreId = savedInstanceState.getInt(STORE_ID);
      presenter = (P) getStore().get(mStoreId);
      if (presenter == null) {
        presenter = createPresenter();
      } else {
        mCallback.setRetainedPresenter(presenter);
      }
    } else {
      presenter = createPresenter();
    }
    presenter.takeView(mCallback.viewComponent());
  }

  private P createPresenter() {
    mStoreId = getStore().generateId();
    P presenter = mCallback.presenter();
    getStore().put(mStoreId, presenter);
    return presenter;
  }

  public void onDestroy() {
    mCallback.presenter().dropView();
    mCallback.presenter().release();
    mActivityRef.clear();
    mActivityRef = null;
  }

  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

  }

  public void onDestroyView() {

  }

  public void onActivityCreated(Bundle savedInstanceState) {

  }

  public void onAttach(Activity activity) {

  }

  public void onDetach() {

  }

  public void onStart() {

  }

  public void onStop() {

  }

  public void onResume() {

  }

  public void onPause() {

  }

  public void onSaveInstanceState(Bundle outState) {
    outState.putInt(STORE_ID, mStoreId);
  }

  @Nullable
  private PresenterStoreFragment getStoreContainer() {
    if (getActivity() instanceof FragmentActivity) {
      FragmentActivity fragmentActivity = (FragmentActivity) getActivity();
      return (PresenterStoreFragment) fragmentActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }
    return (PresenterStoreFragment) getActivity().getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
  }

  @NonNull
  private PresenterStoreFragment createStoreContainer() {
    if (getActivity() instanceof FragmentActivity) {
      // FragmentActivity => support fragment
      final FragmentActivity fragmentActivity = (FragmentActivity) getActivity();
      final RetainedSupportFragment fragment = new RetainedSupportFragment();
      fragmentActivity.getSupportFragmentManager()
              .beginTransaction()
              .add(fragment, FRAGMENT_TAG)
              .commit();
      return fragment;
    }
    // Normal activity => normal fragment
    final RetainedFragment fragment = new RetainedFragment();
    getActivity().getFragmentManager()
            .beginTransaction()
            .add(fragment, FRAGMENT_TAG)
            .commit();
    return fragment;
  }

  private PresenterStoreFragment getStoreFragment() {
    PresenterStoreFragment fragmentStore = getStoreContainer();
    if (fragmentStore == null) {
      fragmentStore = createStoreContainer();
    }
    return fragmentStore;
  }

  @VisibleForTesting
  PresenterStore getStore() {
    return getStoreFragment().store();
  }

  private RetainableStore getRetainable() {
    return getStoreFragment().retainable();
  }

  @NonNull
  private Activity getActivity() {
    if (mActivityRef == null || mActivityRef.get() == null) {
      throw new RuntimeException("Activity was null or not present");
    }
    return mActivityRef.get();
  }

  @Override
  public void retainObject(String objectId, Object object) {
    getRetainable().retainObject(objectId, object);
  }

  @Override
  public Object restoreRetained(String objectId) {
    return getRetainable().restoreRetained(objectId);
  }

  /**
   * Common structure for holding presenters and generating their ids
   */
  @VisibleForTesting
  static final class PresenterStore {
    private static AtomicInteger sCount = new AtomicInteger(1);
    private SparseArrayCompat<Presenter> mRetainedObjects = new SparseArrayCompat<>();

    void put(int key, Presenter presenter) {
      mRetainedObjects.put(key, presenter);
    }

    Presenter get(int key) {
      return mRetainedObjects.get(key);
    }

    void remove(int key) {
      mRetainedObjects.remove(key);
    }

    int generateId() {
      return sCount.incrementAndGet();
    }

    void release() {
      mRetainedObjects.clear();
      mRetainedObjects = null;
    }
  }

  /**
   * Contains a presenter store
   */
  private interface PresenterStoreFragment {
    PresenterStore store();

    RetainableStore retainable();
  }

  /**
   * Headless fragment for handling our cache
   */
  public static final class RetainedFragment extends android.app.Fragment implements PresenterStoreFragment {

    private final PresenterStore mStore;
    private final RetainableStoreImpl mRetainable;

    public RetainedFragment() {
      mStore = new PresenterStore();
      mRetainable = new RetainableStoreImpl();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
      mStore.release();
      super.onDestroy();
    }

    @Override
    public PresenterStore store() {
      return mStore;
    }

    @Override
    public RetainableStore retainable() {
      return mRetainable;
    }
  }

  /**
   * Headless support fragment handling our cache
   */
  public static final class RetainedSupportFragment extends Fragment implements PresenterStoreFragment {

    private final PresenterStore mStore;
    private final RetainableStoreImpl mRetainable;

    public RetainedSupportFragment() {
      mStore = new PresenterStore();
      mRetainable = new RetainableStoreImpl();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
      mStore.release();
      super.onDestroy();
    }

    @Override
    public PresenterStore store() {
      return mStore;
    }

    @Override
    public RetainableStore retainable() {
      return mRetainable;
    }
  }
}


