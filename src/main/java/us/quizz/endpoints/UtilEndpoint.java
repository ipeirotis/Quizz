package us.quizz.endpoints;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.UserReferal;
import us.quizz.utils.PMF;
import us.quizz.utils.UrlUtils;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class UtilEndpoint {

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "util.resaveUserReferals", path="util/resaveUserReferals")
	public void resaveUserReferals() {
		PersistenceManager pm = PMF.getPM();
		try{
			Query query = pm.newQuery(UserReferal.class);
			List<UserReferal> list = (List<UserReferal>)query.execute();
			for(UserReferal ref : list){
				if(ref.getReferer() != null)
					ref.setDomain(UrlUtils.extractDomain(ref.getReferer().getValue()));
			}
			pm.makePersistentAll(list);
		}finally{
			pm.close();
		}
	} 

}