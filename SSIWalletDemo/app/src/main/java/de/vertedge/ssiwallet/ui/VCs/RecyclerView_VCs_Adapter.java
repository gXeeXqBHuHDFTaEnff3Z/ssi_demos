package de.vertedge.ssiwallet.ui.VCs;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.WriterException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.vertedge.ssiwallet.MainActivity;
import de.vertedge.ssiwallet.R;
import de.vertedge.ssiwallet.data.SSI.SSI_Authority;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.data.SSI.SSI_Identity;
import de.vertedge.ssiwallet.data.SSI.SSI_VerifiableCredential;

public class RecyclerView_VCs_Adapter extends RecyclerView.Adapter<RecyclerView_VCs_Adapter.ViewHolder> {

    private List<SSI_VerifiableCredential> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context _context;
    private ArrayList<Long> selectedVCuids = new ArrayList<>();

    // data is passed into the constructor
    public RecyclerView_VCs_Adapter(Context context, List<SSI_VerifiableCredential> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this._context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SSI_VerifiableCredential _vc = mData.get( position );
        String _claim = _vc.get_claim();
        String _owner = _context.getResources().getString(R.string.nobody);
        String _signature = _vc.get_proof();
        long _id = _vc.get_credentialSubject();

        // wenn Identität zugeordnet, dann diese nennen, sonst: nobody
        if (_id != -1){
            SSI_Database db = SSI_Database.getInstance(_context);
            SSI_Identity _identity = db.identityDao().findByUID(_id);
            _owner = _identity.getFullName();
            _owner = String.format("%s: %s",
                    _context.getResources().getString(R.string.owner),
                    _owner);
        } else {
            _owner = String.format("%s: %s",
                    _context.getResources().getString(R.string.owner),
                    _context.getResources().getString(R.string.nobody));
        }

        // Datumsangaben
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(_context);
        Instant issuedDate = _vc.get_issuance();
        Instant validUntil = _vc.get_expiration();
        String _issuedStr = "";
        String _validUntilStr = "";
        if (issuedDate != null){
            _issuedStr = _context.getString( R.string.valid_since ) + ": " + issuedDate + ", ";
        } else {
            _issuedStr = _context.getString( R.string.valid_since ) + ": -, ";
        }
        if (validUntil != null){
            _validUntilStr = _context.getString( R.string.valid_until) + ": " + validUntil;
        } else {
            _validUntilStr = _context.getString( R.string.valid_until ) + ": -";
        }
        final String _datumsangaben = _issuedStr + _validUntilStr;

        // Signaturprüfung DEMO HARDCODED
        SSI_Database db = SSI_Database.getInstance(_context);
        ArrayList<SSI_Authority> authorities = new ArrayList<>();
        authorities.addAll( db.ssiAuthorityDao().getAll() );

        boolean validated = false;
        for (SSI_Authority authority : authorities){
            if (_vc.isValid(authority) ) {
                validated = true;
            }
        }

        String validationResult = (validated ? "✓" : "?");
        String _signatureString = String.format("%s: [%s]",
                _context.getResources().getString(R.string.signature),
                validationResult);
        holder._txtVSignature.setTag(_signature);
        holder._txtVSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert view.getTag() != null;
                assert !view.getTag().toString().isEmpty();

                String _text = view.getTag().toString();
                _text = _datumsangaben + "\n" + _text;
                Toast.makeText(_context, _text, Toast.LENGTH_LONG).show();
            }
        });

        // fill views with parameters
        holder._txtVName.setText( _claim );
        holder._txtVValue.setText( _owner );
        holder._txtVSignature.setText( _signatureString );
        if ( _vc.get_picture() > 0 ){
            holder._imgVPicture.setImageDrawable( ContextCompat.getDrawable(holder._imgVPicture.getContext(), _vc.get_picture()) );
        }

        // bei VCSELECT die checkbox vorbereiten
        if (_context.getClass().equals(MainActivity.class)){
            holder._checkbox.setVisibility(View.GONE);
        } else {
            holder._checkbox.setTag( _vc.get_uid() );
            holder._checkbox.setOnClickListener(view -> {
                CheckBox cb = (CheckBox) view;
                long uid = (long) view.getTag();
                if (!cb.isSelected()){
                    selectedVCuids.add( uid );
                    cb.setSelected( true );
                } else {
                    selectedVCuids.remove( uid );
                    cb.setSelected( false );
                }
            });
        }

        // show qr codes in MainActivity, but not in Signing Activity
        if (_context.getClass().equals(MainActivity.class)){
            // setting this dimensions inside our qr code
            // encoder to generate our qr code.
            Bitmap bitmap;
            QRGEncoder qrgEncoder;
            int dimen = _context.getResources().getInteger(R.integer.qrcode); // was 500

            qrgEncoder = new QRGEncoder(_vc.toJSON(), null, QRGContents.Type.TEXT, dimen);
            try {
                // getting our qrcode in the form of bitmap.
                bitmap = qrgEncoder.encodeAsBitmap();
                // the bitmap is set inside our image
                // view using .setimagebitmap method.
                holder._qrcode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                // this method is called for
                // exception handling.
                Log.e("Tag", e.toString());
            }
        } else holder._qrcode.setVisibility(View.GONE);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView _txtVName;
        final TextView _txtVValue;
        final TextView _txtVSignature;
        final ImageView _imgVPicture;
        final ImageView _qrcode;
        final CheckBox _checkbox;

        // multiselect variables
        boolean isSelected = false;
        long vcuid;

        ViewHolder(View itemView) {
            super(itemView);
            _txtVName = itemView.findViewById(R.id.tvRecyclerName);
            _txtVValue = itemView.findViewById(R.id.tcRecyclerValue);
            _txtVSignature = itemView.findViewById(R.id.tvRecyclerSignature);
            _imgVPicture = itemView.findViewById(R.id.imgV_IDs_avatar);
            _qrcode = itemView.findViewById(R.id.imgV_IDs_qrcode);
            _checkbox = itemView.findViewById(R.id.checkRecyclerView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    SSI_VerifiableCredential getItem(int id) {
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

    public ArrayList<Long> getSelectedVCuids() {
        return selectedVCuids;
    }
}
