package com.example.repoviewer.Repositories;

import com.example.repoviewer.Models.Repo;
import com.example.repoviewer.Responses.RepoResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class RepoRepository implements IRepoRepository {

    private final RestTemplate restTemplate;

    public RepoRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<RepoResponse> getRepositoriesByUsername(String username) {
        String apiUrl = "https://api.github.com/users/{username}/repos".replace("{username}", username);
        Repo[] repositories = restTemplate.getForObject(apiUrl, Repo[].class);
        List<RepoResponse> repoResponses = new ArrayList<>();

        if(repositories != null) {
            for(Repo repository : repositories) {
                repoResponses.add(new RepoResponse(
                    repository.full_name(),
                    getMostUsedLanguage(repository.languages_url()),
                    repository.visibility(),
                    repository.html_url()
                ));
            }
        }

        return repoResponses;
    }

    @Override
    public String getMostUsedLanguage(String languagesUrl) {
        ParameterizedTypeReference<Map<String, Integer>> responseType = new ParameterizedTypeReference<>() {};
        RequestEntity<Void> request = RequestEntity.get(languagesUrl).accept(MediaType.APPLICATION_JSON).build();
        Map<String, Integer> response = restTemplate.exchange(request, responseType).getBody();

        String language = "";
        int maxValue = Integer.MIN_VALUE;

        if(response != null) {
            for (Map.Entry<String, Integer> entry : response.entrySet()) {
                if (entry.getValue() > maxValue) {
                    maxValue = entry.getValue();
                    language = entry.getKey();
                }
            }
        }

        return language;
    }
}
