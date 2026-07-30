package logic_core.app.dto.validator;

import logic_core.app.dto.media.UploadFile;
import logic_core.app.dto.request.UpdateCompleteProfileRequest;
import logic_core.common.exception.NotFoundException;

public class UserValidator
{

    public void validateAllProfile(UpdateCompleteProfileRequest request)
    {
        UsernameValidator.validate(request.username());
        validateBio(request.bio());

        if (request.avatar() != null)
        {
            validateAvatar(request.avatar());
        }

        if (request.banner() != null)
        {
            validateCover(request.banner());
        }
    }


    public void validateSearchUser(String query, int page, int pageSize)
    {
        if(query.length() < 2)
        {
            throw new IllegalStateException("query length.");
        }

        if(page <= 0 || page > 50)
        {
            throw new IllegalStateException("page bounds");
        }

        if(pageSize< 0)
        {
            throw new IllegalStateException("page size.");
        }
    }


    public void validateAvatar(UploadFile file)
    {
        if(file == null)
            throw new NotFoundException("UploadFile most not be null");

        if(!file.contentType().startsWith("image/"))
            throw new IllegalStateException("Only images allowed");

        if(file.data().length > 5 * 1024 * 1024)
            throw new IllegalStateException("Max size is 5MB");

    }

    public void validateBio(String bio)
    {
        if(bio == null)
        {
            throw new IllegalStateException("bio must not be null");
        }

        if(bio.length() > 160)
        {
            throw new IllegalStateException("bio length bounds");
        }
    }

    public void validateCover(UploadFile file)
    {
        if(file == null)
          throw new IllegalStateException( "Cover is required.");


        if(!file.contentType().startsWith("image/"))
        {
            throw new IllegalStateException("Cover must be image.");
        }


        if(file.data().length > 10 * 1024 * 1024)
        {
            throw new IllegalStateException("Cover size cannot exceed 10MB.");
        }
    }
}
