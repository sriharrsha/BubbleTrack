package com.sriharrsha.bubbletrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;
import com.sriharrsha.bubbletrack.R;
import com.sriharrsha.bubbletrack.models.Product;

public class ProductAdapter extends FirestoreRecyclerAdapter<Product, ProductAdapter.ProductDetailHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */

    public ProductAdapter(@NonNull FirestoreRecyclerOptions<Product> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductDetailHolder holder, int position, @NonNull Product model) {
        Picasso.get().load(model.getImageUrl()).into(holder.productImage);
        holder.productName.setText(model.getProductName());
        holder.maxPrice.setText(String.valueOf(model.getMaxPrice()));
        holder.sellingPrice.setText(String.valueOf(model.getSellingPrice()));
    }

    @NonNull
    @Override
    public ProductDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductDetailHolder(view);
    }

    class ProductDetailHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView maxPrice;
        TextView sellingPrice;
        ImageView productImage;

        public ProductDetailHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            maxPrice = itemView.findViewById(R.id.maxPrice);
            sellingPrice = itemView.findViewById(R.id.sellingPrice);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }
}
