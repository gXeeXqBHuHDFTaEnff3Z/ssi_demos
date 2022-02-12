package de.vertedge.ssicapabilitymanager.ui.home;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.vertedge.ssicapabilitymanager.MainActivity;
import de.vertedge.ssicapabilitymanager.R;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.CapMgmt_Database;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Message;

public class RecyclerView_Messages_Adapter extends RecyclerView.Adapter<RecyclerView_Messages_Adapter.ViewHolder> {

    private final List<de.vertedge.ssicapabilitymanager.capabilitymgmt.Message> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final Context _context;
    private CapMgmt_Database db;

    // data is passed into the constructor
    public RecyclerView_Messages_Adapter(Context context, List<Message> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this._context = context;
        this.db = CapMgmt_Database.getInstance( context );
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_messages_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        db = CapMgmt_Database.getInstance( _context );
        // get message data
        Message _msg = mData.get(position);

        Log.d(this.toString(), "Listing position " + position + " with message ID" + _msg.get_uid() + "and signature " + _msg.get_signature());

        String _from = _msg.get_from_user();
        _from = _context.getResources().getString(R.string.from, _from);
        String _msgBody = _msg.get_text();
        ArrayList<Long> msg_unsignedCapabilities = _msg.get_unsignedCapabilities();
        ArrayList<Long> msg_signedCapabilities = _msg.get_signedCapabilities();
        String _attachement = "";
        int typefaceUnread = (_msg.is_unread() ? Typeface.BOLD : Typeface.NORMAL);

        // tag the buttons with the messages id
        holder._bDeny.setTag(_msg.get_uid());
        holder._bGotoSigning.setTag(_msg.get_uid());

        // find the attachment string we need to show
        if (_msg.get_signature() != null) {
            // attached you find signed caps
            for (long _capability : msg_signedCapabilities){
                String capstr = db.capDao().get( _capability ).get_name();
                _attachement = _attachement+  "•" + capstr + "\n";
            }
            holder._bGotoSigning.setText(_context.getString(R.string.send_to_ssi_app));
            holder._txtVAttachmentIs.setText(_context.getString(R.string.attachment_signed));
        } else {
            // attached you find a list of claimed capabilities
            for (long _capability : msg_unsignedCapabilities){
                String capstr = db.capDao().get( _capability ).get_name();
                _attachement = _attachement+  "•" + capstr + "\n";
            }
            holder._bGotoSigning.setText(_context.getString(R.string.goto_signing));
            holder._txtVAttachmentIs.setText(_context.getString(R.string.attachment_unsigned));
        }

        // falls zu signierendes attachement vorhanden, dann anzeigen
        int attachmentVisibility = ( (_msg.get_signature() != null) || !_attachement.isEmpty() ? View.VISIBLE : View.GONE);

        // falls job application, dann statt ablage / signieren die verifizierung anbieten
        boolean isJobApplication = _msg.is_isJobApplication();
        if (isJobApplication){
            holder._bGotoSigning.setText(R.string.goto_details);
        }

        // now set the views to their variables
        holder._txtVFrom.setText(_from);
        holder._txtVFrom.setTypeface(null, typefaceUnread);
        holder._txtVMsgBody.setText(_msgBody);
        holder._layoutAttachment.setVisibility(attachmentVisibility);
        holder._txtVAttachment.setText(_attachement);
        holder._txtVAttachment.setChecked( _msg.get_signature() != null );
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView _txtVFrom;
        TextView _txtVMsgBody;
        CheckedTextView _txtVAttachment;
        TextView _txtVAttachmentIs;
        Button _bDeny;
        Button _bGotoSigning;
        LinearLayout _layoutAttachment;

        ViewHolder(View itemView) {
            super(itemView);
            _txtVFrom = itemView.findViewById(R.id.tvRecyclerFrom);
            _txtVMsgBody = itemView.findViewById(R.id.tvRecyclerText);
            _txtVAttachment = itemView.findViewById(R.id.tvRecyclerMsg_CapsChecked);
            _bDeny = itemView.findViewById(R.id.bRecyclerDeny);
            _bGotoSigning = itemView.findViewById(R.id.bRecyclerGotoSigning);
            _layoutAttachment = itemView.findViewById(R.id.layoutRecMessages_attachment);
            _txtVAttachmentIs = itemView.findViewById(R.id.tVRecyclerMsgAttachmentIs);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Message getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
