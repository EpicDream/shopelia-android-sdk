package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.BaseModel;

/**
 * A list activity able to list any type of resource, select resources, edit
 * remove or add a resource. The activity just need to know on witch mode it is
 * based.
 * 
 * @author Pierre Pollastri
 */
public class ResourceListActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "ResourceListActivity";

    /**
     * The identifier of the resource
     */
    public static final String EXTRA_RESOURCE = Config.EXTRA_PREFIX + "RESOURCE_IDENTIFIER";

    /**
     * The options enabled
     */
    public static final String EXTRA_OPTIONS = Config.EXTRA_PREFIX + "OPTIONS";

    /**
     * The list to display
     */
    public static final String EXTRA_LIST = Config.EXTRA_PREFIX + "LIST";

    public static final int OPTION_ADD = 0x1;
    public static final int OPTION_DELETE = OPTION_ADD << 1;
    public static final int OPTION_EDIT = OPTION_DELETE << 1;
    public static final int OPTION_SELECT = OPTION_EDIT << 1;
    public static final int OPTION_ALL = OPTION_ADD | OPTION_DELETE | OPTION_EDIT | OPTION_SELECT;

    public interface OnItemSelectedListener {
        public void onItemSelected(BaseModel item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_resource_list_activity);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, ResourceListFragment.newInstance(getIntent().getExtras()));
        ft.commit();
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

}
