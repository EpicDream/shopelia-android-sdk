package com.shopelia.android.test.unit;

import java.util.HashSet;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import com.shopelia.android.model.JsonData.JsonInflater;
import com.shopelia.android.utils.MultiHashSet;

public class MultiHashSetTests extends TestCase {

    MultiHashSet<String, String> mhs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mhs = new MultiHashSet<String, String>();
    }

    public void testDiff() {
        for (int i = 5; i < 10; i++) {
            mhs.put("first", "Hi Shopelia " + i);
        }
        for (int i = 0; i < 12; i++) {
            mhs.put("second", "Hi Shopelia " + i);
        }
        HashSet<String> diff = mhs.diff("second", "first");
        assertEquals("Should have 7 differences : ", 7, diff.size());
        assertTrue("Should have the item 'Hi Shopelia 2'", diff.contains("Hi Shopelia 2"));
        assertFalse("Should not have the item 'Hi Shopelia 26'", diff.contains("Hi Shopelia 6"));
    }

    public void testMerge() {
        for (int i = 5; i < 10; i++) {
            mhs.put("first", "Hi Shopelia " + i);
        }
        for (int i = 0; i < 12; i++) {
            mhs.put("second", "Hi Shopelia " + i);
        }
        mhs.merge("second", "first");
        HashSet<String> diff = mhs.diff("second", "first");
        assertEquals("Should have no difference : ", 0, diff.size());
    }

    public void testRevert() {
        for (int i = 5; i < 10; i++) {
            mhs.put("first", "Hi Shopelia " + i);
        }
        for (int i = 0; i < 12; i++) {
            mhs.put("second", "Hi Shopelia " + i);
        }
        mhs.merge("second", "first");
        mhs.revert("second", "first");
        HashSet<String> diff = mhs.diff("second", "first");
        assertEquals("Should have 7 differences : ", 7, diff.size());
    }

    public void testSerialization() throws JSONException {
        for (int i = 5; i < 10; i++) {
            mhs.put("first", "Hi Shopelia " + i);
        }
        for (int i = 0; i < 12; i++) {
            mhs.put("second", "Hi Shopelia " + i);
        }
        JSONObject object = mhs.toJson();
        MultiHashSet<String, String> newMhs = MultiHashSet.inflate(object, JsonInflater.STRING_INFLATER);
        assertEquals(mhs.getSet("first").size(), newMhs.getSet("first").size());
        assertEquals(mhs.getSet("second").size(), newMhs.getSet("second").size());
        mhs.put("third", newMhs.getSet("first"));
        mhs.put("fourth", newMhs.getSet("second"));
        HashSet<String> diff = mhs.diff("fourth", "first");
        assertTrue("Should have the item 'Hi Shopelia 2'", diff.contains("Hi Shopelia 2"));
        assertTrue("Should have no difference", mhs.diff("first", "third").size() == 0);
        assertTrue("Should have no difference", mhs.diff("second", "fourth").size() == 0);
    }
}
