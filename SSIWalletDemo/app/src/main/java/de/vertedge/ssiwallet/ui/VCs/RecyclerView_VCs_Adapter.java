package de.vertedge.ssiwallet.ui.VCs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.zxing.WriterException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.vertedge.ssiwallet.DetailsActivity;
import de.vertedge.ssiwallet.MainActivity;
import de.vertedge.ssiwallet.R;
import de.vertedge.ssiwallet.SigningActivity;
import de.vertedge.ssiwallet.data.SSI.SSI_Authority;
import de.vertedge.ssiwallet.data.SSI.SSI_Claim;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.data.SSI.SSI_Identity;
import de.vertedge.ssiwallet.data.SSI.SSI_Representation;
import de.vertedge.ssiwallet.data.SSI.SSI_VerifiableCredential;

public class RecyclerView_VCs_Adapter extends RecyclerView.Adapter<RecyclerView_VCs_Adapter.ViewHolder> {

    private final List<SSI_VerifiableCredential> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final Context _context;
    private final boolean selectionView;
    private final ArrayList<Long> selectedVCuids = new ArrayList<>();

    // data is passed into the constructor
    public RecyclerView_VCs_Adapter(Context context, List<SSI_VerifiableCredential> data, boolean selectionView) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this._context = context;
        this.selectionView = selectionView;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.rcview_vc_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SSI_VerifiableCredential _vc = mData.get( position );
        String _title = (_vc.get_claims().size() > 0 ? _vc.get_claims().get(0).get_value() : _vc.get_context());
        String _claim = "";
        for (SSI_Claim claim : _vc.get_claims()){
            _claim = _claim + "•" + claim.toString() + "\n";
            _claim = _claim.replace("confirmed", _context.getString(R.string.confirms));
        }
        String _owner = _context.getResources().getString(R.string.nobody);
        String _signature = _vc.get_proof();
        long _id = _vc.get_credentialSubject();

        // top bar color
        _vc.assertColor(_context);
        holder._layoutTop.setBackgroundColor(_vc.get_color());

        // bei OnClick die Details anzeigen
        if (!selectionView)
        holder._cardView.setOnClickListener(view -> {
            SSI_Representation rep = new SSI_Representation(_vc, "");

            Intent details = new Intent(_context, DetailsActivity.class);
            details.setAction(Intent.ACTION_ASSIST);
            details.setType("application/ssi");
            details.putExtra(SigningActivity.EXTRA_REP_JSON, rep.toJSON());
            _context.startActivity( details );
        });

        // wenn Identität zugeordnet, dann diese nennen, sonst: nobody
        if (_id != -1){
            SSI_Database db = SSI_Database.getInstance(_context);
            SSI_Identity _identity = db.identityDao().findByUID(_id);
            _owner = _identity.getFullName();
            _claim = _claim.replace(_identity.getDID().toLowerCase(Locale.ROOT), _identity.getFullName());
        } else {
            _owner = _context.getResources().getString(R.string.nobody);
        }

        // Datumsangaben
        DateTimeFormatter dtf = DateTimeFormatter
                .ofLocalizedDate( FormatStyle.SHORT )
                .withLocale(_context.getResources().getConfiguration().locale)
                .withZone( ZoneId.systemDefault() );

        Instant issuedDate = _vc.get_issuance();
        Instant validUntil = _vc.get_expires();
        String _issuedStr;
        String _validUntilStr;
        if (issuedDate != null){
            _issuedStr = _context.getString( R.string.valid_since ) + ": " + dtf.format( issuedDate ) + ", ";
        } else {
            _issuedStr = _context.getString( R.string.valid_since ) + ": -, ";
        }
        if (validUntil != null){
            _validUntilStr = _context.getString( R.string.valid_until) + ": " + dtf.format(validUntil);
        } else {
            _validUntilStr = _context.getString( R.string.valid_until ) + ": -";
        }
        final String _datumsangaben = _issuedStr + _validUntilStr;

        // Signaturprüfung DEMO HARDCODED
        SSI_Database db = SSI_Database.getInstance(_context);
        ArrayList<SSI_Authority> authorities = new ArrayList<>(db.ssiAuthorityDao().getAll());

        // go through all authorities and check if it signed this
        boolean validated = false;
        for (SSI_Authority authority : authorities){
            if (_vc.isValid(authority) ) {
                validated = true;
            }
        }

        // if authorities did not sign this, maybe one of our ids?
        if (!validated){
            List<SSI_Identity> identities = db.identityDao().getAll();
            for (SSI_Identity identity : identities){
                SSI_Representation rep = new SSI_Representation(_vc, "");
                if ( rep.isValid() ) {
                    validated = true;
                }
            }
        }

        String validationResult = (validated ? "✓" : "?");
        String _signatureString = String.format("[%s]",
                validationResult);
        holder._txtVSignature.setText( _signatureString );
        holder._txtVSignature.setTag(_signature);

        if (!selectionView)
        holder._txtVSignature.setOnClickListener(view -> {
            assert view.getTag() != null;
            assert !view.getTag().toString().isEmpty();

            String _text = view.getTag().toString();
            _text = _datumsangaben + "\n" + _text;
            Toast.makeText(_context, _text, Toast.LENGTH_LONG).show();
        });

        // copy sig to clipbaord button
        holder._imgBcopySig.setTag(_signature);
        holder._imgBcopySig.setOnClickListener(view -> {
            assert view.getTag() != null;
            assert !view.getTag().toString().isEmpty();

            String _text = view.getTag().toString();
            _text = _datumsangaben + "\n" + _text;

            ClipboardManager clipboard = (ClipboardManager) _context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text/plain", _text);
            clipboard.setPrimaryClip(clip);

            String _infotext = String.format("%s %s",
                    _context.getString(R.string.signature),
                    _context.getString(R.string.copied_to_clipbaord));
            Toast.makeText(_context, _infotext, Toast.LENGTH_LONG).show();
        });

        // fill views with parameters
        holder._txtVName.setText( _title );
        holder._txtVValue.setText( _claim );
        holder._txtVOwner.setText( _owner );

        // icon
        if (_vc.get_iconID() > 0) try{
            Drawable dr = ResourcesCompat.getDrawable(_context.getResources(), _vc.get_iconID(), null);
            holder._imgVPicture.setImageDrawable( dr );
        } catch (Exception e){
            // images doesnt work then
            e.printStackTrace();
            holder._imgVPicture.setVisibility(View.GONE);
        }
        if (selectionView){
          holder._imgVPicture.setVisibility(View.GONE);
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
        if ( !selectionView ){
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
        final CardView _cardView;
        final LinearLayout _layoutTop;
        final TextView _txtVName;
        final TextView _txtVValue;
        final TextView _txtVOwner;
        final TextView _txtVSignature;
        final ImageView _imgVPicture;
        final ImageView _qrcode;
        final ImageButton _imgBcopySig;
        final CheckBox _checkbox;

        // multiselect variables
        boolean isSelected = false;
        long vcuid;

        ViewHolder(View itemView) {
            super(itemView);
            _cardView = itemView.findViewById(R.id.cardview);
            _layoutTop = itemView.findViewById(R.id.row_vc_layoutTop);
            _txtVName = itemView.findViewById(R.id.tvRecyclerName);
            _txtVValue = itemView.findViewById(R.id.tcRecyclerValue);
            _txtVOwner = itemView.findViewById(R.id.txtVrcVCOwner);
            _txtVSignature = itemView.findViewById(R.id.tvRecyclerSignature);
            _imgVPicture = itemView.findViewById(R.id.imgV_IDs_avatar);
            _qrcode = itemView.findViewById(R.id.imgV_IDs_qrcode);
            _checkbox = itemView.findViewById(R.id.checkRecyclerView);
            _imgBcopySig = itemView.findViewById(R.id.rcVC_sig_copy);
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
