package de.vertedge.ssicapabilitymanager.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.vertedge.ssicapabilitymanager.MainActivity;
import de.vertedge.ssicapabilitymanager.R;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.CapMgmt_Database;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Message;
import de.vertedge.ssicapabilitymanager.databinding.FragmentMessagesBinding;

public class FragmentMessages extends Fragment implements MainActivity.OnPermitDenyListener {

    private HomeViewModel homeViewModel;
    private FragmentMessagesBinding binding;
    private TextView tvNoNewMessages;
    private RecyclerView_Messages_Adapter adapter;
    private MainActivity main;
    private CapMgmt_Database db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        main = (MainActivity) getActivity();
        assert main != null;
        main.registerListener( this );
        tvNoNewMessages = root.findViewById(R.id.tv_msg_no_new_msg);

        db = CapMgmt_Database.getInstance(getContext());
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Context context = binding.getRoot().getContext();

        final RecyclerView recyclerView = binding.recVMessages;
        // create grid layout with the appropriate number of recycler columns
        final int columns = getResources().getInteger(R.integer.recyclerview_columns);
        GridLayoutManager _layout = new GridLayoutManager( context, columns );
        recyclerView.setLayoutManager( _layout );

        String _user= "";
        // select messages this user can see
        if (main.get_currentUser() != null){
            _user = "ssi:" + main.get_currentUser().get_ssidid();
        }

        // get messages and check if there are any
        List<Message> messages = CapMgmt_Database.getUsersMessages(context, _user);

        if (messages.size() == 0){
            tvNoNewMessages.setVisibility(View.VISIBLE);
        } else {
            tvNoNewMessages.setVisibility(View.GONE);
            // create adapter with only those messages that are for the current user
            adapter = new RecyclerView_Messages_Adapter(context, messages);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void OnDenyClick(long  uid) {
        // if the user denys a message, ask him if he wants to delete it
        @SuppressLint("NotifyDataSetChanged") DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            Message oldmsg = db.messagesDao().get(uid);
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    String _text = getString(R.string.message_denied_reply);
                    String _from = oldmsg.get_to_user();
                    String _to = oldmsg.get_from_user();
                    Message newmsg = new Message(_text,
                            _from,
                            _to,
                            oldmsg.get_unsignedCapabilities(),
                            oldmsg.get_signedCapabilities(),
                            oldmsg.get_signature(),
                            true,
                            false,
                            null);
                    db.messagesDao().insert( newmsg );
                    db.messagesDao().delete( oldmsg );
                    adapter.notifyDataSetChanged();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    db.messagesDao().delete( oldmsg );
                    adapter.notifyDataSetChanged();
                    break;
            }
        };

        String question = getString(R.string.msgbox_message_denied_reply);
        String yes = getString(R.string.yes);
        String no = getString(R.string.no);
        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setMessage(question).setPositiveButton(yes, dialogClickListener)
                .setNegativeButton(no, dialogClickListener).show();
    }

    @Override
    public void OnGotoSigningClick(long uid) {
        // refill adapter
        onStart();
    }

}