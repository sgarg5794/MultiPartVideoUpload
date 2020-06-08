/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class AnEmptyController {



	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	List<Video> videoList = new ArrayList<>();
	Map<Long,MultipartFile> map = new HashedMap();
	public static final AtomicLong atomicInteger = new AtomicLong(0L);


	@RequestMapping(path = "/video",method = RequestMethod.GET)
	@ResponseBody
	public List<Video> getVideos(){
		return videoList;
	}



		@RequestMapping(path = VideoSvcApi.VIDEO_SVC_PATH,method = RequestMethod.POST
			,consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Video postVideo(@RequestBody Video video){
		long videoId=atomicInteger.incrementAndGet();
		video.setId(videoId);
		video.setDataUrl(getDataUrl(videoId));
		videoList.add(video);
		return video;
	}

	@RequestMapping(path = VideoSvcApi.VIDEO_DATA_PATH,method = RequestMethod.POST)
		public ResponseEntity<VideoStatus> postVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
										@RequestParam(VideoSvcApi.DATA_PARAMETER)MultipartFile multipartFile ){
		Video video=null;
		try {
			Optional vid=videoList.stream().filter(v->v.getId()==id).findFirst();
			if(!vid.isPresent()){
				return new ResponseEntity(HttpStatus.NOT_FOUND);
			}
			VideoFileManager videoFileManager=VideoFileManager.get();
			InputStream inputStream = multipartFile.getInputStream();
			video=(Video)vid.get();
			videoFileManager.saveVideoData(video,inputStream);

			map.put(video.getId(),multipartFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity(new VideoStatus(VideoStatus.VideoState.READY),HttpStatus.OK);
	}

	@RequestMapping(path = VideoSvcApi.VIDEO_DATA_PATH,method = RequestMethod.GET)
	public ResponseEntity<Resource> getVideoData(@PathVariable("id") long id){
		Path filePath = Paths.get("src/test/resources/").resolve(
				String.format("upload%d.mp4", id)).normalize();
		ByteArrayResource resource = null;
		try {
			resource = new ByteArrayResource(Files.readAllBytes(filePath));
		} catch (IOException e) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity(resource, HttpStatus.OK);
	}

	private String getDataUrl(long videoId){
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}

	private String getUrlBaseForLocalServer() {
		HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String base =
				"http://"+request.getServerName()
						+ ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		return base;
	}











}
