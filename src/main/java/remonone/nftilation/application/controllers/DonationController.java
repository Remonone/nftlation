package remonone.nftilation.application.controllers;

import remonone.nftilation.application.models.Donation;
import remonone.nftilation.application.models.PlayerCredentials;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.utils.HttpRequestMethod;
import remonone.nftilation.utils.ResponseBody;
import remonone.nftilation.utils.annotations.BodyContent;
import remonone.nftilation.utils.annotations.EndPointListener;

public class DonationController extends BaseController {

    @EndPointListener(path="/receive_donation", method = HttpRequestMethod.POST)
    public ResponseBody<String> receiveDonation(@BodyContent Donation donation) {
        ActionContainer.InitAction(donation.type, donation.donationParameters);
        return ResponseBody.createOKResponse("Successfully received donation");
    }

    @EndPointListener(path = "/test", method = HttpRequestMethod.POST)
    public ResponseBody<String> test(@BodyContent PlayerCredentials credentials) {
        return ResponseBody.createOKResponse(credentials.password);
    }

}