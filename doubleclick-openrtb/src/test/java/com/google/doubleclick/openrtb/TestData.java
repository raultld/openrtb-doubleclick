/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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
 */

package com.google.doubleclick.openrtb;

import static java.lang.Math.min;

import com.google.common.collect.ImmutableList;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.MatchingAdData;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.MatchingAdData.BuyerPricingRule;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.SlotVisibility;
import com.google.protos.adx.NetworkBid.BidRequest.Hyperlocal;
import com.google.protos.adx.NetworkBid.BidRequest.HyperlocalSet;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile.DeviceOsVersion;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile.MobileDeviceType;
import com.google.protos.adx.NetworkBid.BidRequest.UserDataTreatment;
import com.google.protos.adx.NetworkBid.BidRequest.UserDemographic;
import com.google.protos.adx.NetworkBid.BidRequest.Vertical;
import com.google.protos.adx.NetworkBid.BidRequest.Video;
import com.google.protos.adx.NetworkBid.BidRequest.Video.CompanionSlot;
import com.google.protos.adx.NetworkBid.BidRequest.Video.CompanionSlot.CreativeFormat;
import com.google.protos.adx.NetworkBid.BidRequest.Video.VideoFormat;

import java.util.List;

public class TestData {
  static final int NO_SLOT = -1;

  public static Bid.Builder newBid(boolean size) {
    Bid.Builder bid = Bid.newBuilder()
        .setId("0")
        .setImpid("1")
        .setAdid("2")
        .setCrid("4")
        .setPrice(1.2)
        .setAdm("<blink>hello world</blink>");
    if (size) {
      bid.setCid("3");
      bid.setDealid("5");
      bid.setW(200);
      bid.setH(220);
    }
    return bid;
  }

  public static NetworkBid.BidRequest newRequest() {
    return newRequest(0, false).build();
  }

  static List<Integer> createSizes(int size, int base) {
    ImmutableList.Builder<Integer> sizes = ImmutableList.builder();
    for (int i = 0; i < size; ++i) {
      sizes.add(base + i);
    }
    return sizes.build();
  }

  @SafeVarargs
  static <T> List<T> sublist(int size, T... items) {
    ImmutableList.Builder<T> sizes = ImmutableList.builder();
    for (int i = 0; i < min(size, items.length); ++i) {
      sizes.add(items[i]);
    }
    return sizes.build();
  }

  public static NetworkBid.BidRequest.Builder newRequest(int size, boolean coppa) {
    NetworkBid.BidRequest.Builder req = NetworkBid.BidRequest.newBuilder()
        .setId(TestUtil.REQUEST_ID)
        .setGoogleUserId("john")
        .setConstrainedUsageGoogleUserId("j")
        .setHostedMatchData(ByteString.copyFrom(new byte[]{
            (byte) 0xEC, (byte) 0x22, (byte) 0xE6, (byte) 0x9C,
            (byte) 0xC8, (byte) 0xB0, (byte) 0x4A, (byte) 0xCA,
            (byte) 0xBB, (byte) 0x6C, (byte) 0xD4, (byte) 0xDA,
            (byte) 0x88, (byte) 0xFB, (byte) 0x33, (byte) 0xB6
        }))
        .setConstrainedUsageHostedMatchData(ByteString.EMPTY)
        .addAllDetectedContentLabel(sublist(size, 40, 41, 999))
        .addAllDetectedLanguage(sublist(size, "en", "en_US", "pt", "pt_BR"))
        .addAllDetectedVertical(sublist(size,
            Vertical.newBuilder().setId(1).setWeight(0.25f).build(),
            Vertical.newBuilder().setId(99).setWeight(0.33f).build(),
            Vertical.newBuilder().setId(2).setWeight(0.75f).build(),
            Vertical.newBuilder().setId(99).setWeight(0.99f).build()));
    if (size == 1) {
      req
          .setIp(ByteString.copyFrom(new byte[] { (byte) 192, (byte) 168, (byte) 1 } ))
          .setUserAgent("Chrome")
          .setGeoCriteriaId(9058770)
          .setAnonymousId("mysite.com")
          .setSellerNetworkId(1);
    } else if (size == 2) {
      req
          .setIp(ByteString.copyFrom(new byte[] {
              0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x1F, 0x2F, 0x3F, 0x4F, 0x5F, 0x6F } ))
          .setUrl("mysite.com/newsfeed")
          .setPostalCode("10011")
          .setUserDemographic(UserDemographic.newBuilder()
              .setGender(UserDemographic.Gender.FEMALE)
              .setAgeLow(18)
              .setAgeHigh(24))
          .setEncryptedHyperlocalSet(ByteString.copyFrom(new byte[]{1,2,3} /* bad */));
    } else if (size == 3) {
      req
          .setUserDemographic(UserDemographic.newBuilder())
          .setPostalCodePrefix("100")
          .setEncryptedHyperlocalSet(ByteString.copyFrom(
              new DoubleClickCrypto.Hyperlocal(TestUtil.KEYS).encryptHyperlocal(
                  HyperlocalSet.newBuilder()
                      .setCenterPoint(Hyperlocal.Point.newBuilder()
                          .setLatitude(45)
                          .setLongitude(90))
                      .build().toByteArray(), new byte[16])));
    } else if (size >= 4) {
      req
          .setGeoCriteriaId(0 /* bad */)
          .setEncryptedHyperlocalSet(ByteString.copyFrom(
              new DoubleClickCrypto.Hyperlocal(TestUtil.KEYS).encryptHyperlocal(
                  HyperlocalSet.newBuilder().build().toByteArray(), new byte[16])));
    }
    if (size != NO_SLOT) {
      AdSlot.Builder adSlot = AdSlot.newBuilder()
          .setId(1)
          .addAllWidth(createSizes(size, 100))
          .addAllHeight(createSizes(size, 200))
          .addAllAllowedVendorType(sublist(size, 10, 94, 97))
          .addAllExcludedSensitiveCategory(sublist(size, 0, 3, 4))
          .addAllExcludedAttribute(sublist(size, 1, 2, 3, 32 /* MraidType: Mraid 1.0 */))
          .addAllExcludedProductCategory(sublist(size, 1, 2, 999));
      for (int i = 1; i < size; ++i) {
        adSlot
            .setAdBlockKey(i)
            .setSlotVisibility(SlotVisibility.ABOVE_THE_FOLD)
            .addTargetableChannel(size % 2 == 0 ? "afv_user_id_PewDiePie" : "pack-anon-x::y");
        MatchingAdData.Builder mad = MatchingAdData.newBuilder()
            .setAdgroupId(100 + i);
        if (i >= 2) {
          mad.setMinimumCpmMicros(10000 + i);
          for (int j = 2; j <= i; ++j) {
            MatchingAdData.DirectDeal.Builder deal = MatchingAdData.DirectDeal.newBuilder()
                .setDirectDealId(10 * i + j);
            if (j >= 3) {
              deal.setFixedCpmMicros(1200000);
            }
            mad.addDirectDeal(deal);

            BuyerPricingRule.Builder rule = BuyerPricingRule.newBuilder();
            if (j >= 3) {
              rule.setMinimumCpmMicros(1200000);
            }
            mad.addPricingRule(rule);
          }
        }
        adSlot.addMatchingAdData(mad);
      }
      req.addAdslot(adSlot);
    }
    if (coppa) {
      req.addUserDataTreatment(UserDataTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT);
    }
    return req;
  }

  static Mobile.Builder newMobile(int size) {
    Mobile.Builder mobile = Mobile.newBuilder();
    if (size % 2 == 0) {
      mobile
          .setAppId("com.mygame")
          .setMobileDeviceType(MobileDeviceType.HIGHEND_PHONE)
          .setOsVersion(DeviceOsVersion.newBuilder()
              .setOsVersionMajor(3).setOsVersionMinor(2).setOsVersionMicro(1))
          .setModel("MotoX")
          .setEncryptedHashedIdfa(ByteString.EMPTY)
          .setConstrainedUsageEncryptedHashedIdfa(ByteString.EMPTY)
          .setAppName("Tic-Tac-Toe")
          .setAppRating(4.2f)
          .setIsInterstitialRequest(true);
    }
    return mobile;
  }

  static Video.Builder newVideo(int size) {
    Video.Builder video = Video.newBuilder()
        .addAllAllowedVideoFormats(sublist(size, VideoFormat.VIDEO_FLASH, VideoFormat.VIDEO_HTML5))
        .setMinAdDuration(15)
        .setMaxAdDuration(60);
    if (size != NO_SLOT) {
      CompanionSlot.Builder compSlot = CompanionSlot.newBuilder()
          .addAllWidth(createSizes(size, 100))
          .addAllHeight(createSizes(size, 200));
      if (size >= 2) {
        video.setVideoadStartDelay(5);
        compSlot.addCreativeFormat(CreativeFormat.IMAGE_CREATIVE);
      }
      video.addCompanionSlot(compSlot);
    }
    return video;
  }
}
