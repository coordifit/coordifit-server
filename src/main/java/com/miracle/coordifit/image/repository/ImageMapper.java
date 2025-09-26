package com.miracle.coordifit.image.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.miracle.coordifit.image.model.Image;

@Mapper
public interface ImageMapper {
	void insertImage(Image image);

	List<Image> selectAllImages();

}
