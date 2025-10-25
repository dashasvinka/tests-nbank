package api.requests.skelethon.interfaces;

import api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);

    Object put(BaseModel model);
    Object get(int id);
    Object get();
    Object update(long id, BaseModel model);
    Object delete(long id);
}
