package de.vertedge.ssicapabilitymanager.ui.slideshow;

import android.content.Context;
import android.graphics.Paint;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import de.vertedge.ssicapabilitymanager.MainActivity;
import de.vertedge.ssicapabilitymanager.R;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.CapMgmt_Database;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Capability;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Joblisting;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Organisation;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.User;

public class RecyclerView_Capabilities_Adapter extends RecyclerView.Adapter<RecyclerView_Capabilities_Adapter.ViewHolder> implements View.OnClickListener {

    private final List<Capability> mData;
    private final LayoutInflater mInflater;
    private RecyclerView_Capabilities_Adapter.ItemClickListener mClickListener;
    private final Context _context;
    private final User _currentUser;
    CapMgmt_Database db;

    // data is passed into the constructor
    public RecyclerView_Capabilities_Adapter(Context context, List<Capability> data, User user) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this._context = context;
        this._currentUser = user;
        db = CapMgmt_Database.getInstance(_context);
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView_Capabilities_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_capabilities_row, parent, false);
        return new RecyclerView_Capabilities_Adapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView_Capabilities_Adapter.ViewHolder holder, int position) {
        Capability _cap = mData.get( position );
        String _capname = _cap.get_name();
        String _state = "";
        if (_cap.get_state() == Capability.CapState.VOTING){
            Instant deadline = _cap.get_deadline();
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
                            .withLocale( _context.getResources().getConfiguration().locale )
                            .withZone( ZoneId.systemDefault() );
            _state = String.format("%s (%s: %d, %s: %d) \n %s %s",
                    _cap.get_state(),
                    _context.getString(R.string.yes),
                    _cap.get_votesYes(),
                    _context.getString(R.string.no),
                    _cap.get_votesNo(),
                    _context.getString(R.string.cap_deadline),
                    formatter.format( deadline ));
        } else {
            // no voting
            _state = String.format("%s: %s", _context.getString(R.string.cap_state), _cap.get_state().name());
            holder._bVoteYes.setVisibility(View.GONE);
            holder._bVoteNo.setVisibility(View.GONE);
        }
        holder._txtVName.setText(_capname);
        holder._txtVDescription.setText(_cap.get_description());

        // STATE
        holder._txtVState.setText(_state);

        // VOTING
        holder._bVoteYes.setOnClickListener( this );
        holder._bVoteYes.setTag(position);
        holder._bVoteNo.setOnClickListener( this );
        holder._bVoteNo.setTag(-position);

        // check if user already voted and disable buttons
        if (_currentUser.getCapsVotedOn().contains(_cap.get_uid())){
          holder._bVoteYes.setVisibility(View.GONE);
          holder._bVoteNo.setVisibility(View.GONE);
          String text = holder._txtVState.getText().toString();
          text = text + "\n" + _context.getString(R.string.cap_voted);
          holder._txtVState.setText(text);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /** called when the buttons for voting are pressed
     *
     * @param view Voting Button
     */
    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        Log.d("Button.onClick", "Clicked position : " + position);
        // <0 means NO, else YES
        if ( view.getId() == R.id.bVoteNo){
            // NO clicked
            Capability cap = mData.get(position);

            // VOTE and remember the user voting
            db.capDao().voteNo(cap.get_uid());
            _currentUser.getCapsVotedOn().add(cap.get_uid());
            db.usersDao().insert(_currentUser);

            mData.set(position, db.capDao().get(cap.get_uid()));
            this.notifyDataSetChanged();
        } else {
            // YES clicked
            Capability cap = mData.get(position);

            // VOTE and remember the user voting
            db.capDao().voteNo(cap.get_uid());
            _currentUser.getCapsVotedOn().add(cap.get_uid());
            db.usersDao().insert(_currentUser);

            mData.set(position, db.capDao().get(cap.get_uid()));
            this.notifyDataSetChanged();
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView _txtVName;
        TextView _txtVDescription;
        TextView _txtVState;
        Button _bVoteYes;
        Button _bVoteNo;

        ViewHolder(View itemView) {
            super(itemView);
            _txtVName = itemView.findViewById(R.id.tvRecycler_Capabilities_Name);
            _txtVDescription = itemView.findViewById(R.id.tVRecycler_Caps_descritpion);
            _txtVState = itemView.findViewById(R.id.tvRecycler_Caps_state);
            _bVoteYes = itemView.findViewById(R.id.bVoteYes);
            _bVoteNo = itemView.findViewById(R.id.bVoteNo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Capability getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(RecyclerView_Capabilities_Adapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
