package uk.ac.man.cs.eventlite.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import java.time.Instant;
import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
public class MastodonPost {
    private String id;
    
    private String content;
    private String url;
    
    private Date createdAt;
    private String formattedTime;
    private String formattedDate;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    
    @JsonProperty("created_at")
    public void setCreatedAtViaString(String createdAt) {
        this.setCreatedAt(Date.from(Instant.parse(createdAt)));
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        formatDateTime(createdAt);
    }

    private void formatDateTime(Date date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        this.formattedTime = timeFormat.format(date);
        this.formattedDate = dateFormat.format(date);
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content.replaceAll("<.*?>", "");;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getFormattedTime() {
        return formattedTime;
    }

    public String getFormattedDate() {
        return formattedDate;
    }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "MastodonPost{" +
               "id='" + id + '\'' +
               ", createdAt=" + (createdAt != null ? sdf.format(createdAt) : "null") +
               ", content='" + (content != null ? content.replaceAll("\n", " ").replaceAll("\r", " ") : "null") + '\'' +
               ", url='" + url + '\'' +
               '}';
    }
}
