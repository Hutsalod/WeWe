package chat.wewe.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.wewe.android.BuildConfig;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.activity.StatusConnect;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.AbstractFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.wewe.android.fragment.sidebar.dialog.FragmentTask;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.layouthelper.chatroom.roomlist.RoomListAdapter;
import chat.wewe.android.layouthelper.chatroom.roomlist.RoomListHeader;
import chat.wewe.android.renderer.UserRenderer;
import chat.wewe.core.interactors.RoomInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.models.RoomSidebar;
import chat.wewe.core.models.Spotlight;
import chat.wewe.core.models.User;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmSpotlightRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import hugo.weaving.DebugLog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class SidebarMainFragment extends AbstractFragment implements SidebarMainContract.View, StatusConnect {
    private SidebarMainContract.Presenter presenter;
    public  MethodCallHelper methodCallHelper;
    private RoomListAdapter adapter;
    private SwipeController swipeController = null;
    private SearchView searchView;
    private TextView loadMoreResultsText;
    private List<RoomSidebar> roomSidebarList = Collections.emptyList();
    private Disposable spotlightDisposable;
    private String hostname;
    private String nameUsers = "1";
    public ImageView stanUsers;
    private static final String HOSTNAME = "hostname";

    public SidebarMainFragment() {
    }

    /**
     * build SidebarMainFragment with hostname.
     */
    public static SidebarMainFragment create(String hostname) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);


        SidebarMainFragment fragment = new SidebarMainFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hostname = getArguments().getString(HOSTNAME);
        RealmUserRepository userRepository = new RealmUserRepository(hostname);

        AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname))
        );


        presenter = new SidebarMainPresenter(
                hostname,
                new RoomInteractor(new RealmRoomRepository(hostname)),
                userRepository,
                absoluteUrlHelper,
                new MethodCallHelper(getContext(), hostname),
                new RealmSpotlightRepository(hostname)
        );

        methodCallHelper = new MethodCallHelper(getContext(), hostname);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        presenter.bindView(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        presenter.release();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();

    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_sidebar_main;
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    protected void onSetupView() {
        setupUserActionToggle();
        setupLogoutButton();
        setupVersionInfo();
        openDialog();
        searchView = rootView.findViewById(R.id.search);
        stanUsers = rootView.findViewById(R.id.stanUsers);
        EditText searchEditText = (EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.parseColor("#909092"));
        searchEditText.setHintTextColor(Color.parseColor("#909092"));
        searchEditText.setTextSize(14);
        adapter = new RoomListAdapter();
        adapter.setOnItemClickListener(new RoomListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RoomSidebar roomSidebar) {
                searchView.setQuery(null, false);
                searchView.clearFocus();
                presenter.onRoomSelected(roomSidebar);

                if (nameUsers == roomSidebar.getRoomName()) {
                    ((MainActivity) getActivity()).pane.closePane();
                } else {
                    nameUsers = roomSidebar.getRoomName();
                }


            }

            @Override
            public void onItemClick(Spotlight spotlight) {
                searchView.setQuery(null, false);
                searchView.clearFocus();
                presenter.onSpotlightSelected(spotlight);
            }
        });

        RecyclerView recyclerView = rootView.findViewById(R.id.room_list_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                methodCallHelper.deleteRooms(adapter.arrayListList.get(position),adapter.arrayListList.get(position).substring(0,17));
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, adapter.getItemCount());
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c,bmp);
            }
        });

        loadMoreResultsText = rootView.findViewById(R.id.text_load_more_results);

        RxSearchView.queryTextChanges(searchView)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    if (spotlightDisposable != null && !spotlightDisposable.isDisposed()) {
                        spotlightDisposable.dispose();
                    }
                    presenter.disposeSubscriptions();
                    if (charSequence.length() == 0) {
                        loadMoreResultsText.setVisibility(View.GONE);
                        adapter.setMode(RoomListAdapter.MODE_ROOM);
                        presenter.bindView(this);
                    } else {
                        filterRoomSidebarList(charSequence);
                    }
                });

        loadMoreResultsText.setOnClickListener(view -> loadMoreResults());



    }

    @Override
    public void showRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList) {
        this.roomSidebarList = roomSidebarList;
        adapter.setRoomSidebarList(roomSidebarList);
    }

    @Override
    public void filterRoomSidebarList(CharSequence term) {
        List<RoomSidebar> filteredRoomSidebarList = new ArrayList<>();

        for (RoomSidebar roomSidebar : roomSidebarList) {
            if (roomSidebar.getRoomName().contains(term)) {
                filteredRoomSidebarList.add(roomSidebar);
            }
        }

        if (filteredRoomSidebarList.isEmpty()) {
            loadMoreResults();
        } else {
            loadMoreResultsText.setVisibility(View.VISIBLE);
            adapter.setMode(RoomListAdapter.MODE_ROOM);
            adapter.setRoomSidebarList(filteredRoomSidebarList);
        }
    }

    private void loadMoreResults() {
        spotlightDisposable = presenter.searchSpotlight(searchView.getQuery().toString())
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showSearchSuggestions);
    }

    private void showSearchSuggestions(List<Spotlight> spotlightList) {
        loadMoreResultsText.setVisibility(View.GONE);
        adapter.setMode(RoomListAdapter.MODE_SPOTLIGHT);
        adapter.setSpotlightList(spotlightList);
    }

    @SuppressLint("RxLeakedSubscription")
    private void setupUserActionToggle() {
        final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        toggleUserAction.setFocusableInTouchMode(false);

        rootView.findViewById(R.id.user_info_container).setOnClickListener(view -> toggleUserAction.toggle());

        RxCompoundButton.checkedChanges(toggleUserAction)
                .compose(bindToLifecycle())
                .subscribe(
                        this::showUserActionContainer,
                        Logger.INSTANCE::report
                );
    }

    public void showUserActionContainer(boolean show) {
        rootView.findViewById(R.id.user_action_outer_container)
                .setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void toggleUserActionContainer(boolean checked) {
        CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        toggleUserAction.setChecked(checked);
    }

    @Override
    public void showScreen() {
        rootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyScreen() {
        rootView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void show(User user) {
      //  onRenderCurrentUser(user);
        updateRoomListMode();

    }



    private void updateRoomListMode() {
        final List<RoomListHeader> roomListHeaders = new ArrayList<>();
        adapter.setRoomListHeaders(roomListHeaders);
    }

    @DebugLog
    @Override
    public void onPreparedToLogOut() {
        final Activity activity = getActivity();
        if (activity != null && activity instanceof MainActivity) {
            ((MainActivity) activity).onLogout();
        }
    }

    private void setupLogoutButton() {
        rootView.findViewById(R.id.btn_logout).setOnClickListener(view -> {
            closeUserActionContainer();
            // Clear relative data and set new hostname if any.
            presenter.prepareToLogOut();
        });
    }

    public void clearSearchViewFocus() {
        searchView.clearFocus();
    }

    public void closeUserActionContainer() {
        final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        if (toggleUserAction != null && toggleUserAction.isChecked()) {
            toggleUserAction.setChecked(false);
        }
    }

    private void setupVersionInfo() {
        TextView versionInfoView = rootView.findViewById(R.id.version_info);
        versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
    }

    private void showAddRoomDialog(DialogFragment dialog) {
        dialog.show(getFragmentManager(), "AbstractAddRoomDialogFragment");
    }

    public void openDialog() {
        rootView.findViewById(R.id.btnCreate).setOnClickListener(view -> {
            AddChannelDialogFragment.create(hostname).show(getActivity().getSupportFragmentManager(), "example dialog");
         //   ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.sidebar_fragment_container, AddChannelDialogFragment.create(hostname))
         //          .commit();
        });
    }


    @Override
    public void noConnect() {
        if(stanUsers!=null)
        stanUsers.setImageResource(R.drawable.s000);
    }

    @Override
    public void Connecting() {
        if(stanUsers!=null)
        this.stanUsers.setImageResource(R.drawable.s112);
    }

    @Override
    public void okConnect() {
        if(stanUsers!=null)
        this.stanUsers.setImageResource(R.drawable.s222);
    }
}