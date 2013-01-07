/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.datadelivery.subscription;

import java.util.List;

import com.raytheon.uf.common.datadelivery.registry.AdhocSubscription;
import com.raytheon.uf.common.datadelivery.registry.InitialPendingSubscription;
import com.raytheon.uf.common.datadelivery.registry.Subscription;
import com.raytheon.uf.common.registry.handler.RegistryHandlerException;
import com.raytheon.uf.viz.datadelivery.subscription.SubscriptionService.IForceApplyPromptDisplayText;

/**
 * Services for working with subscriptions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 7, 2012  1286      djohnson     Initial creation
 * Nov 20, 2012 1286      djohnson     Use IDisplay to display yes/no prompt.
 * Nov 28, 2012 1286      djohnson     Consolidate more notifications.
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */

public interface ISubscriptionService {

    /**
     * Contract for the return object of subscription service actions.
     */
    public static interface ISubscriptionServiceResult {
        /**
         * Check whether further editing should occur.
         * 
         * @return true if the UI should not be closed, and the user should be
         *         allowed to resubmit their action
         */
        boolean isAllowFurtherEditing();

        /**
         * Check whether there is a message to display.
         * 
         * @return true if there is a message to display
         */
        boolean hasMessageToDisplay();

        /**
         * Return the message to display. Should not be called unless
         * {@link #hasMessageToDisplay()} returns true.
         * 
         * @return the message to display
         */
        String getMessageToDisplay();
    }

    /**
     * Store the subscription.
     * 
     * @param subscription
     *            the subscription to store
     * @param displayTextStrategy
     * @return the result object
     * @throws RegistryHandlerException
     */
    ISubscriptionServiceResult store(Subscription subscription,
            IForceApplyPromptDisplayText displayTextStrategy)
            throws RegistryHandlerException;

    /**
     * Update the subscription.
     * 
     * @param subscription
     *            the subscription to update
     * @param displayTextStrategy
     * @return the result object
     */
    ISubscriptionServiceResult update(Subscription subscription,
            IForceApplyPromptDisplayText displayTextStrategy)
            throws RegistryHandlerException;

    /**
     * Update the subscriptions.
     * 
     * @param subscriptions
     *            the subscriptions to update
     * @param displayTextStrategy
     * @return the result object
     */
    ISubscriptionServiceResult update(List<Subscription> subscriptions,
            IForceApplyPromptDisplayText displayTextStrategy)
            throws RegistryHandlerException;

    /**
     * Store the adhoc subscription.
     * 
     * @param subscription
     *            the subscription to store
     * @param display
     *            the display to use to prompt the user
     * @param displayTextStrategy
     * @return the result object
     * @throws RegistryHandlerException
     */
    public ISubscriptionServiceResult store(AdhocSubscription sub,
            IForceApplyPromptDisplayText displayTextStrategy)
            throws RegistryHandlerException;

    /**
     * Send a notification that pending subscription was created.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendCreatedPendingSubscriptionNotification(
            InitialPendingSubscription subscription, String username);

    /**
     * Send a notification that a subscription was created.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendCreatedSubscriptionNotification(Subscription subscription,
            String username);

    /**
     * Send a notification that a subscription was updated.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendUpdatedSubscriptionNotification(Subscription subscription,
            String username);

    /**
     * Send a notification that a pending subscription was updated.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendUpdatedPendingSubscriptionNotification(
            InitialPendingSubscription subscription, String username);

    /**
     * Send a notification that a subscription update is pending.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendCreatedPendingSubscriptionForSubscriptionNotification(
            InitialPendingSubscription pendingSub, String username);

    /**
     * Send a notification that a pending subscription was approved.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendApprovedPendingSubscriptionNotification(
            InitialPendingSubscription subscription, String username);

    /**
     * Send a notification that a pending subscription was denied.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     * @param denyMessage
     *            the reason for denying
     */
    void sendDeniedPendingSubscriptionNotification(
            InitialPendingSubscription subscription, String username,
            String denyMessage);

    /**
     * Send a notification that the subscription was deleted.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendDeletedSubscriptionNotification(Subscription subscription,
            String username);

    /**
     * Send a notification that the subscription was activated.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendSubscriptionActivatedMessage(Subscription subscription,
            String username);

    /**
     * Send a notification that the subscription was deactivated.
     * 
     * @param subscription
     *            the subscription
     * @param username
     *            the username
     */
    void sendSubscriptionDeactivatedMessage(Subscription subscription,
            String username);
}
