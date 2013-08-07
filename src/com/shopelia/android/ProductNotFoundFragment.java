package com.shopelia.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Product;
import com.shopelia.android.widget.FontableTextView;

public class ProductNotFoundFragment extends ShopeliaFragment<Void> {

    private static final String ARGS_PRODUCT = "args:product";

    public static ProductNotFoundFragment newInstance(Product product) {
        ProductNotFoundFragment fragment = new ProductNotFoundFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_PRODUCT, product);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_product_not_found_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Product product = getArguments().getParcelable(ARGS_PRODUCT);
        FontableTextView text = findViewById(R.id.text);
        text.setText(getString(R.string.shopelia_product_not_fount_text, product.merchant.name));
        findViewById(R.id.validate).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = product.url;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                if (getShowsDialog()) {
                    getDialog().cancel();
                }
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

}
