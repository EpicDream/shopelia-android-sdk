package com.shopelia.android.test.remote.api;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.shopelia.android.model.Address;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.AddressesAPI;
import com.shopelia.android.remote.api.AddressesAPI.OnAddressEvent;
import com.shopelia.android.remote.api.AddressesAPI.OnEditAddressEvent;
import com.shopelia.android.remote.api.AddressesAPI.OnRequestDone;
import com.shopelia.android.remote.api.ApiController.OnApiErrorEvent;
import com.shopelia.android.test.TestUtils;
import com.shopelia.android.test.model.AddressFactory;
import com.shopelia.android.test.model.UserFactory;

public class AddressesAPITest extends InstrumentationTestCase {

    User user;
    Address address;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = TestUtils.signUp(getInstrumentation().getTargetContext(), UserFactory.get("test"));
        address = AddressFactory.get("second");
    }

    public void testAddAddress() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        AddressesAPI api = new AddressesAPI(getInstrumentation().getTargetContext());
        api.register(new Object() {

            public void onEventMainThread(OnAddressEvent event) {
                result.set(true);
                barrier.countDown();
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });
        api.addAddress(address);
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
        AddressesAPI api = new AddressesAPI(getInstrumentation().getTargetContext());
        api.register(new Object() {

            public void onEventMainThread(OnEditAddressEvent event) {
                result.set(true);
                barrier.countDown();
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });
        api.editAddress(address);
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
        AddressesAPI api = new AddressesAPI(getInstrumentation().getTargetContext());
        api.register(new Object() {

            public void onEventMainThread(OnRequestDone event) {
                barrier.countDown();
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });
        api.setDefaultAddress(toDefault);
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
