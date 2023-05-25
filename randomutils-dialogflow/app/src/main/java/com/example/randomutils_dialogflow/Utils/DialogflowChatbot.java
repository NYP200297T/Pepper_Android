package com.example.randomutils_dialogflow.Utils;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.ReplyPriority;
import com.aldebaran.qi.sdk.object.conversation.StandardReplyReaction;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.example.randomutils_dialogflow.DialogRequest;

import static com.aldebaran.qi.sdk.object.conversation.ReplyPriority.FALLBACK;

/**
 * A sample chatbot that delegates questions/answers to a Dialogflow agent.
 */
public class DialogflowChatbot extends BaseChatbot {

    private static final String TAG = "DialogflowChatbot";
    private static final String EXCITEMENT_ACTION = "excitement";

    DialogflowChatbot(final QiContext context) {
        super(context);
    }

    @Override
    public StandardReplyReaction replyTo(final Phrase phrase, final Locale locale) {

        Log.d(TAG,"Heard phrase: "+phrase.getText());

        if (phrase.getText().isEmpty()) {
            EmptyChatbotReaction emptyReac = new EmptyChatbotReaction(getQiContext());
            return new StandardReplyReaction(emptyReac, ReplyPriority.FALLBACK);
        } else {
            // Ask the online DialogFlow agent to answer to the phrase
            try {
                // Return a reply built from the agent's response
                String response = DialogRequest.request(phrase.getText());
                return replyFromAIResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void acknowledgeHeard(final Phrase phrase, final Locale locale) {
        Log.i(TAG, "The robot heard: "+ phrase.getText());
    }

    @Override
    public void acknowledgeSaid(final Phrase phrase, final Locale locale) {
        Log.i(TAG, "The robot uttered this reply, provided by another chatbot: "+ phrase.getText());
    }

    /*
     * Build a reply that can be processed by our chatbot, based on the response from Dialogflow
     */
    private StandardReplyReaction replyFromAIResponse(final String answer) {
        Log.d(TAG, "replyFromAIResponse");

        // Extract relevant data from Dialogflow response
        // final Result result = response.getResult();
        // String answer       = result.getFulfillment().getSpeech();
        // String intentName   = result.getMetadata().getIntentName();
        // String action       = result.getAction();

        // Set the priority of our reply, here by detecting the fallback nature of the Dialogflow
        // response according the name of the intent that was triggered
        BaseChatbotReaction reaction;
        if (answer != null) {
            reaction = new ChatbotUtteredReaction(getQiContext(), answer);
        } else {
            reaction = new ChatbotUtteredReaction(getQiContext(), "Sorry, I don't understand, please try again.");
        }

        // Make the reply and return it
        return new StandardReplyReaction(reaction, FALLBACK);
    }
}
