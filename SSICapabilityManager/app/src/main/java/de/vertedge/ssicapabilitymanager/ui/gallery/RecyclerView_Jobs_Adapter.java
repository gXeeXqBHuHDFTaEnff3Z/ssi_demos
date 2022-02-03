package de.vertedge.ssicapabilitymanager.ui.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.vertedge.ssicapabilitymanager.MainActivity;
import de.vertedge.ssicapabilitymanager.R;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.CapMgmt_Database;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Capability;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Joblisting;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Organisation;
import de.vertedge.ssicapabilitymanager.ui.home.RecyclerView_Messages_Adapter;

public class RecyclerView_Jobs_Adapter extends RecyclerView.Adapter<RecyclerView_Jobs_Adapter.ViewHolder> {

    interface OnDataChangedListener{
        void OnDataChanged();
    }

    private final List<Joblisting> mData;
    private final LayoutInflater mInflater;
    private RecyclerView_Messages_Adapter.ItemClickListener mClickListener;
    private final OnDataChangedListener mDataListener;
    private final Context _context;
    CapMgmt_Database db;

    // data is passed into the constructor
    public RecyclerView_Jobs_Adapter(Context context, List<Joblisting> data, OnDataChangedListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this._context = context;
        this.db = CapMgmt_Database.getInstance(_context);
        Log.i(this.toString(), "creating adapter with joblisting size " + data.size());
        mDataListener = listener;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_joblistings_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Joblisting _job = mData.get( position );

        Organisation _org = db.orgsDao().get( _job.get_company_id() );
        String _organisation = _org.get_name();

        // Dates sind kompliziert
        java.util.Date _validUntilDate = _job.get_validUntil();
        String _validUntil = "-";
        if (_validUntilDate != null){
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(_context);
            _validUntil = _context.getString( R.string.valid_until ) + " " + dateFormat.format( _validUntilDate );
        }

        // list all the requirements, first necessary, then optional
        String _requirementsString = "";
        List<Long> _requirements = _job.getMinRequirements();
        for (long _requirement : _requirements){
            String added =  "•" + db.capDao().get( _requirement ).get_name();
            _requirementsString = _requirementsString.concat( added ).concat("\n");
        }

        _requirementsString = _requirementsString.concat("\nOptional:\n");

        List<Long> _optrequirements = _job.getBonusRequirements();

        if (_optrequirements != null)
        for (long _requirement : _optrequirements){
            String added =  "•" + db.capDao().get( _requirement ).get_name();
            _requirementsString = _requirementsString.concat( added ).concat("\n");
        }

        // linking
        String linkmsg = String.format("%s: %s",
                _context.getString(R.string.jobs_link),
                _job.get_link());

        // starred / favourite?
        @SuppressLint("UseCompatLoadingForDrawables") Drawable star = _context.getDrawable(_job.is_starred() ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        holder._imgStarred.setImageDrawable(star);
        holder._imgStarred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _job.set_starred(!_job.is_starred());
                db = CapMgmt_Database.getInstance(view.getContext());
                db.jobsDao().update(_job.get_uid(), _job.is_starred());
                mDataListener.OnDataChanged();
            }
        });

        // apply variables to views
        holder._txtVOrg.setText(_organisation);
        holder._txtVvalidUntil.setText(_validUntil);
        holder._txtVrequirements.setText(_requirementsString);
        holder._bApplyForJob.setTag(_job);
        holder._txtVLink.setText(linkmsg);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView _txtVOrg;
        TextView _txtVvalidUntil;
        TextView _txtVLink;
        TextView _txtVrequirements;
        Button _bApplyForJob;
        ImageView _imgStarred;

        ViewHolder(View itemView) {
            super(itemView);
            _txtVOrg = itemView.findViewById(R.id.tvRecycler_Jobs_Organisation);
            _txtVvalidUntil = itemView.findViewById(R.id.tvRecycler_Jobs_validUntil);
            _txtVLink = itemView.findViewById(R.id.tVRecycler_jobs_link);
            _txtVrequirements = itemView.findViewById(R.id.tVRecycler_Jobs_requirements);
            _bApplyForJob = itemView.findViewById(R.id.bRcJobs_ApplyForJob);
            _imgStarred = itemView.findViewById(R.id.imgVRecycler_jobs_star);
            _bApplyForJob.setOnClickListener( this );
            itemView.setOnClickListener( this );
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Joblisting getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(RecyclerView_Messages_Adapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
