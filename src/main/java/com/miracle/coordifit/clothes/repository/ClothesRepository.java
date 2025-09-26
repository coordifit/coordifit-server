package com.miracle.coordifit.clothes.repository;

import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.clothes.model.ClothesImageLink;
import com.miracle.coordifit.common.model.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClothesRepository {

    int insertClothes(Clothes clothes);
    int updateClothes(Clothes clothes);
    int deleteClothes(@Param("clothesId") String clothesId);

    Clothes findById(@Param("clothesId") String clothesId);
    List<Clothes> findAllByUser(@Param("userId") String userId);

    int insertImageLink(ClothesImageLink link);
    int deleteAllImageLinks(@Param("clothesId") String clothesId);
    int deleteImageLink(@Param("clothesId") String clothesId, @Param("fileId") Long fileId);

    List<ClothesImageLink> findImageLinks(@Param("clothesId") String clothesId);

    int countImageLinks(@Param("clothesId") String clothesId);

    List<FileInfo> findImageFiles(@Param("clothesId") String clothesId);
}
