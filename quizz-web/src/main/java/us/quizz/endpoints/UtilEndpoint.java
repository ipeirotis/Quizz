package us.quizz.endpoints;

import java.util.List;

import us.quizz.entities.UserReferal;
import us.quizz.repository.UserReferralRepository;
import us.quizz.utils.UrlUtils;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class UtilEndpoint {
	
	@Inject
	private UserReferralRepository userReferralRepository;

	@ApiMethod(name = "util.resaveUserReferals", path="util/resaveUserReferals")
	public void resaveUserReferals() {
		List<UserReferal> list = userReferralRepository.list();
		for(UserReferal ref : list){
			if(ref.getReferer() != null)
				ref.setDomain(UrlUtils.extractDomain(ref.getReferer().getValue()));
		}
		userReferralRepository.saveAll(list);
	} 

}