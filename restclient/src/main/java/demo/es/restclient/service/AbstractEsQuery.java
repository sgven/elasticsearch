package demo.es.restclient.service;

public abstract class AbstractEsQuery implements EsQuery,DbQuery {

    @Override
    public Object queryDbById(String id) {
        return null;
    }

    @Override
    public Object queryDb() {
        return null;
    }

    @Override
    public Object queryEsById(String id) {
        return null;
    }

    @Override
    public Object queryEs() {
        return null;
    }

    public Object queryUnique(String id) {
        Object retVal = queryEsById(id);
        if (retVal == null) {
            retVal = queryDbById(id);
        }
        return retVal;
    }

    public Object queryAll() {
        Object retVal = queryEs();
        if (retVal == null) {
            retVal = queryDb();
        }
        return retVal;
    }

}
