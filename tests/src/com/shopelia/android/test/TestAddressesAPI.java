package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.shopelia.android.model.Address;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.AddressesAPI;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.test.model.MockAddress;
import com.shopelia.android.test.model.MockUser;
import com.turbomanage.httpclient.HttpResponse;

public class TestAddressesAPI extends InstrumentationTestCase {

    User user;
    Address address;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = TestUtils.signUp(getInstrumentation().getTargetContext(), MockUser.get("test"));
        address = MockAddress.get("second");
    }

    public void testAddAddress() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        new AddressesAPI(getInstrumentation().getTargetContext(), new CallbackAdapter() {

            @Override
            public void onAddressAdded(Address address) {
                super.onAddressAdded(address);
                result.set(true);
                barrier.countDown();
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                barrier.countDown();
            }

        }).addAddress(address);
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        assertTrue("Should returns true", result.get());
        User after = TestUtils.updateUser(getInstrumentation().getTargetContext());
        user = after;
        for (Address a : after.addresses) {
            if (address.address.equals(a.address)) {
                assertTrue(true);
                return;
            }
        }
        fail("Address not found");
    }

    public void testEditAddress() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        address.id = user.getDefaultAddress().id;
        new AddressesAPI(getInstrumentation().getTargetContext(), new CallbackAdapter() {

            @Override
            public void onAddressEdited(Address a) {
                super.onAddressEdited(address);
                result.set(address.address.equals(a.address));
                barrier.countDown();
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                barrier.countDown();
            }

        }).editAddress(address);
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        assertTrue("Should returns true", result.get());
    }

    public void testSetDefaultAddress() {
        testAddAddress();
        Address toDefault = null;
        for (Address address : user.addresses) {
            Log.d(null, "ADDRESS " + address.address + " " + address.is_default);
            if (!address.is_default) {
                toDefault = address;
            }
        }
        if (toDefault == null) {
            fail("No non-default address (must be a bug)");
        }
        final CountDownLatch barrier = new CountDownLatch(1);
        new AddressesAPI(getInstrumentation().getTargetContext(), new CallbackAdapter() {

            @Override
            public void onRequestDone() {
                super.onRequestDone();
                barrier.countDown();
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                barrier.countDown();
            }

        }).setDefaultAddress(toDefault);
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        User u = TestUtils.updateUser(getInstrumentation().getTargetContext());
        assertEquals("Addresses should be the same", toDefault.address, u.getDefaultAddress().address);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
