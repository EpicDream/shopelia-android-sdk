package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.shopelia.android.model.Address;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.AddressesAPI;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.test.model.MockAddress;
import com.shopelia.android.test.model.MockUser;
import com.turbomanage.httpclient.HttpResponse;

public class TestAddressesAPI extends InstrumentationTestCase {

    public void testAddAddress() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        TestUtils.signUp(getInstrumentation().getTargetContext(), MockUser.get("test"));
        Address address = MockAddress.get("second");
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
        User user = TestUtils.signUp(getInstrumentation().getTargetContext(), MockUser.get("test"));
        final Address address = MockAddress.get("second");
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

}
