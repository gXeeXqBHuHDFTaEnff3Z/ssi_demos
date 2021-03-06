package de.vertedge.ssiwallet.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.zxing.WriterException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.vertedge.ssiwallet.R;
import de.vertedge.ssiwallet.data.SSI.SSI_Authority;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.data.SSI.SSI_Identity;

public class RecyclerView_IDs_Adapter extends RecyclerView.Adapter<RecyclerView_IDs_Adapter.ViewHolder> {

    private final Context context;
    private final List<SSI_Identity> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public RecyclerView_IDs_Adapter(Context context, List<SSI_Identity> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SSI_Identity _identity = mData.get(position);
        String name = _identity.get_firstname() + " " + _identity.get_lastname();
        String _signature = _identity.get_signature();
        holder._txtVName.setText(name);

        // Dates sind kompliziert
        DateTimeFormatter dtf = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(context.getResources().getConfiguration().locale)
                .withZone(ZoneId.systemDefault());

        String _birthstring = "";
        Instant birthday = _identity.get_birthday();
        if (birthday != null) {
            _birthstring = context.getString(R.string.birthday) + " " + dtf.format(birthday);
        }

        // Authority
        SSI_Database db = SSI_Database.getInstance(context);
        SSI_Authority ssi_authority = db.ssiAuthorityDao().findByUID(_identity.get_authority());

        String _authority = "";
        if (ssi_authority != null) _authority = ssi_authority.toString();

        // Signaturpr??fung DEMO HARDCODED
        String validationResult;
        if (_identity.get_signature() == null) {
            validationResult = context.getString(R.string.none);
        } else {
            boolean validated = _identity.isSignedBy(SSI_Authority.DEFAULT_AUTHORITY);
            validationResult = (validated ? "???" : "INVALID");
        }

        String _signatureString = String.format("%s: [%s]",
                context.getResources().getString(R.string.signature),
                validationResult);
        holder._txtVSignature.setTag(_signature);
        holder._txtVSignature.setOnClickListener(view -> {
            assert view.getTag() != null;
            assert !view.getTag().toString().isEmpty();

            String _signature1 = view.getTag().toString();
            Toast.makeText(context, _signature1, Toast.LENGTH_LONG).show();
        });

        // views mit werten f??llen
        holder._txtVValue.setText(String.format("%s\n%s", _authority ,_birthstring));
        holder._txtVSignature.setText(_signatureString);
        holder._checkBox.setVisibility(View.GONE);

        if (_identity.get_picture() > 0)
            try {
                holder._imgVPicture.setImageDrawable(ContextCompat.getDrawable(holder._imgVPicture.getContext(), _identity.get_picture()));
            } catch (Resources.NotFoundException e) {
                // no image then
            }

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        Bitmap bitmap;
        QRGEncoder qrgEncoder;
        int dimen = context.getResources().getInteger(R.integer.qrcode);

        qrgEncoder = new QRGEncoder(_identity.toString(), null, QRGContents.Type.TEXT, dimen);
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
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final CardView _cardView;
        final CheckBox _checkBox;
        final TextView _txtVName;
        final TextView _txtVValue;
        final TextView _txtVSignature;
        final ImageView _imgVPicture;
        final ImageView _qrcode;

        ViewHolder(View itemView) {
            super(itemView);
            _cardView = itemView.findViewById(R.id.cardview);
            _checkBox = itemView.findViewById(R.id.checkRecyclerView);
            _txtVName = itemView.findViewById(R.id.tvRecyclerName);
            _imgVPicture = itemView.findViewById(R.id.imgV_IDs_avatar);
            _txtVValue = itemView.findViewById(R.id.tcRecyclerValue);
            _txtVSignature = itemView.findViewById(R.id.tvRecyclerSignature);
            _qrcode = itemView.findViewById(R.id.imgV_IDs_qrcode);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
