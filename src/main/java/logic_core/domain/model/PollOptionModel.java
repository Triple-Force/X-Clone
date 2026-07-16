package logic_core.domain.model;

import Shared.Models.PollOption.PollOption;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PollOptionModel
{
    private UUID optionId;
    private UUID pollId;
    private String text;
    private long voteCount;


    public void incrementVote()
    {
        this.voteCount++;
    }


    public boolean hasValidText()
    {
        return text != null && !text.isBlank() && text.length() <= 100;
    }


    public boolean belongsTo(UUID pollId)
    {
        return this.pollId != null && this.pollId.equals(pollId);
    }


    public void updateText(String newText)
    {

        PollOption pollOption = new PollOption();

        pollOption.getId();
        if (newText == null || newText.isBlank())
        {
            throw new IllegalArgumentException("Option text cannot be empty");
        }
        this.text = newText;
    }
}
