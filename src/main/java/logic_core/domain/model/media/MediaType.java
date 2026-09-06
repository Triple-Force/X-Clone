package logic_core.domain.model.media;

/**
 * Media type enumeration for the domain layer.
 * Moved from infrastructure to domain to maintain proper dependency direction:
 * domain → domain (not domain → infrastructure).
 */
public enum MediaType
{
    IMAGE, VIDEO, GIF
}
