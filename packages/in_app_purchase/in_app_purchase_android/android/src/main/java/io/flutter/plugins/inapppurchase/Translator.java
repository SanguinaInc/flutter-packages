// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.inapppurchase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.billingclient.api.AccountIdentifiers;
import com.android.billingclient.api.AlternativeBillingOnlyReportingDetails;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingConfig;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.UserChoiceDetails;
import com.android.billingclient.api.UserChoiceDetails.Product;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Handles serialization and deserialization of {@link com.android.billingclient.api.BillingClient}
 * related objects.
 */
/*package*/ class Translator {
  static HashMap<String, Object> fromProductDetail(ProductDetails detail) {
    HashMap<String, Object> info = new HashMap<>();
    info.put("title", detail.getTitle());
    info.put("description", detail.getDescription());
    info.put("productId", detail.getProductId());
    info.put("productType", detail.getProductType());
    info.put("name", detail.getName());

    @Nullable
    ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails =
        detail.getOneTimePurchaseOfferDetails();
    if (oneTimePurchaseOfferDetails != null) {
      info.put(
          "oneTimePurchaseOfferDetails",
          fromOneTimePurchaseOfferDetails(oneTimePurchaseOfferDetails));
    }

    @Nullable
    List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList =
        detail.getSubscriptionOfferDetails();
    if (subscriptionOfferDetailsList != null) {
      info.put(
          "subscriptionOfferDetails",
          fromSubscriptionOfferDetailsList(subscriptionOfferDetailsList));
    }

    return info;
  }

  static List<QueryProductDetailsParams.Product> toProductList(
      List<Messages.PlatformProduct> platformProducts) {
    List<QueryProductDetailsParams.Product> products = new ArrayList<>();
    for (Messages.PlatformProduct platformProduct : platformProducts) {
      products.add(toProduct(platformProduct));
    }
    return products;
  }

  static QueryProductDetailsParams.Product toProduct(Messages.PlatformProduct platformProduct) {

    return QueryProductDetailsParams.Product.newBuilder()
        .setProductId(platformProduct.getProductId())
        .setProductType(toProductTypeString(platformProduct.getProductType()))
        .build();
  }

  static String toProductTypeString(Messages.PlatformProductType type) {
    switch (type) {
      case INAPP:
        return BillingClient.ProductType.INAPP;
      case SUBS:
        return BillingClient.ProductType.SUBS;
    }
    throw new Messages.FlutterError("UNKNOWN_TYPE", "Unknown product type: " + type, null);
  }

  static List<Object> fromProductDetailsList(@Nullable List<ProductDetails> productDetailsList) {
    if (productDetailsList == null) {
      return Collections.emptyList();
    }

    // This and the method are generically typed due to Pigeon limitations; see
    // https://github.com/flutter/flutter/issues/116117.
    ArrayList<Object> output = new ArrayList<>();
    for (ProductDetails detail : productDetailsList) {
      output.add(fromProductDetail(detail));
    }
    return output;
  }

  static HashMap<String, Object> fromOneTimePurchaseOfferDetails(
      @Nullable ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails) {
    HashMap<String, Object> serialized = new HashMap<>();
    if (oneTimePurchaseOfferDetails == null) {
      return serialized;
    }

    serialized.put("priceAmountMicros", oneTimePurchaseOfferDetails.getPriceAmountMicros());
    serialized.put("priceCurrencyCode", oneTimePurchaseOfferDetails.getPriceCurrencyCode());
    serialized.put("formattedPrice", oneTimePurchaseOfferDetails.getFormattedPrice());

    return serialized;
  }

  static List<HashMap<String, Object>> fromSubscriptionOfferDetailsList(
      @Nullable List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList) {
    if (subscriptionOfferDetailsList == null) {
      return Collections.emptyList();
    }

    ArrayList<HashMap<String, Object>> serialized = new ArrayList<>();

    for (ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails :
        subscriptionOfferDetailsList) {
      serialized.add(fromSubscriptionOfferDetails(subscriptionOfferDetails));
    }

    return serialized;
  }

  static HashMap<String, Object> fromSubscriptionOfferDetails(
      @Nullable ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails) {
    HashMap<String, Object> serialized = new HashMap<>();
    if (subscriptionOfferDetails == null) {
      return serialized;
    }

    serialized.put("offerId", subscriptionOfferDetails.getOfferId());
    serialized.put("basePlanId", subscriptionOfferDetails.getBasePlanId());
    serialized.put("offerTags", subscriptionOfferDetails.getOfferTags());
    serialized.put("offerIdToken", subscriptionOfferDetails.getOfferToken());

    ProductDetails.PricingPhases pricingPhases = subscriptionOfferDetails.getPricingPhases();
    serialized.put("pricingPhases", fromPricingPhases(pricingPhases));

    return serialized;
  }

  static List<HashMap<String, Object>> fromPricingPhases(
      @NonNull ProductDetails.PricingPhases pricingPhases) {
    ArrayList<HashMap<String, Object>> serialized = new ArrayList<>();

    for (ProductDetails.PricingPhase pricingPhase : pricingPhases.getPricingPhaseList()) {
      serialized.add(fromPricingPhase(pricingPhase));
    }
    return serialized;
  }

  static HashMap<String, Object> fromPricingPhase(
      @Nullable ProductDetails.PricingPhase pricingPhase) {
    HashMap<String, Object> serialized = new HashMap<>();

    if (pricingPhase == null) {
      return serialized;
    }

    serialized.put("formattedPrice", pricingPhase.getFormattedPrice());
    serialized.put("priceCurrencyCode", pricingPhase.getPriceCurrencyCode());
    serialized.put("priceAmountMicros", pricingPhase.getPriceAmountMicros());
    serialized.put("billingCycleCount", pricingPhase.getBillingCycleCount());
    serialized.put("billingPeriod", pricingPhase.getBillingPeriod());
    serialized.put("recurrenceMode", pricingPhase.getRecurrenceMode());

    return serialized;
  }

  static HashMap<String, Object> fromPurchase(Purchase purchase) {
    HashMap<String, Object> info = new HashMap<>();
    List<String> products = purchase.getProducts();
    info.put("orderId", purchase.getOrderId());
    info.put("packageName", purchase.getPackageName());
    info.put("purchaseTime", purchase.getPurchaseTime());
    info.put("purchaseToken", purchase.getPurchaseToken());
    info.put("signature", purchase.getSignature());
    info.put("products", products);
    info.put("isAutoRenewing", purchase.isAutoRenewing());
    info.put("originalJson", purchase.getOriginalJson());
    info.put("developerPayload", purchase.getDeveloperPayload());
    info.put("isAcknowledged", purchase.isAcknowledged());
    info.put("purchaseState", purchase.getPurchaseState());
    info.put("quantity", purchase.getQuantity());
    AccountIdentifiers accountIdentifiers = purchase.getAccountIdentifiers();
    if (accountIdentifiers != null) {
      info.put("obfuscatedAccountId", accountIdentifiers.getObfuscatedAccountId());
      info.put("obfuscatedProfileId", accountIdentifiers.getObfuscatedProfileId());
    }
    return info;
  }

  static HashMap<String, Object> fromPurchaseHistoryRecord(
      PurchaseHistoryRecord purchaseHistoryRecord) {
    HashMap<String, Object> info = new HashMap<>();
    List<String> products = purchaseHistoryRecord.getProducts();
    info.put("purchaseTime", purchaseHistoryRecord.getPurchaseTime());
    info.put("purchaseToken", purchaseHistoryRecord.getPurchaseToken());
    info.put("signature", purchaseHistoryRecord.getSignature());
    info.put("products", products);
    info.put("developerPayload", purchaseHistoryRecord.getDeveloperPayload());
    info.put("originalJson", purchaseHistoryRecord.getOriginalJson());
    info.put("quantity", purchaseHistoryRecord.getQuantity());
    return info;
  }

  static List<Object> fromPurchasesList(@Nullable List<Purchase> purchases) {
    if (purchases == null) {
      return Collections.emptyList();
    }

    // This and the method are generically typed due to Pigeon limitations; see
    // https://github.com/flutter/flutter/issues/116117.
    List<Object> serialized = new ArrayList<>();
    for (Purchase purchase : purchases) {
      serialized.add(fromPurchase(purchase));
    }
    return serialized;
  }

  static List<Object> fromPurchaseHistoryRecordList(
      @Nullable List<PurchaseHistoryRecord> purchaseHistoryRecords) {
    if (purchaseHistoryRecords == null) {
      return Collections.emptyList();
    }

    // This and the method are generically typed due to Pigeon limitations; see
    // https://github.com/flutter/flutter/issues/116117.
    List<Object> serialized = new ArrayList<>();
    for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecords) {
      serialized.add(fromPurchaseHistoryRecord(purchaseHistoryRecord));
    }
    return serialized;
  }

  static Messages.PlatformBillingResult fromBillingResult(BillingResult billingResult) {
    return new Messages.PlatformBillingResult.Builder()
        .setResponseCode((long) billingResult.getResponseCode())
        .setDebugMessage(billingResult.getDebugMessage())
        .build();
  }

  static Messages.PlatformUserChoiceDetails fromUserChoiceDetails(
      UserChoiceDetails userChoiceDetails) {
    return new Messages.PlatformUserChoiceDetails.Builder()
        .setExternalTransactionToken(userChoiceDetails.getExternalTransactionToken())
        .setOriginalExternalTransactionId(userChoiceDetails.getOriginalExternalTransactionId())
        .setProductsJsonList(fromProductsList(userChoiceDetails.getProducts()))
        .build();
  }

  static List<Object> fromProductsList(List<Product> productsList) {
    if (productsList.isEmpty()) {
      return Collections.emptyList();
    }

    // This and the method are generically typed due to Pigeon limitations; see
    // https://github.com/flutter/flutter/issues/116117.
    ArrayList<Object> output = new ArrayList<>();
    for (Product product : productsList) {
      output.add(fromProduct(product));
    }
    return output;
  }

  static HashMap<String, Object> fromProduct(Product product) {
    HashMap<String, Object> info = new HashMap<>();
    info.put("id", product.getId());
    info.put("offerToken", product.getOfferToken());
    info.put("productType", product.getType());

    return info;
  }

  /** Converter from {@link BillingResult} and {@link BillingConfig} to map. */
  static Messages.PlatformBillingConfigResponse fromBillingConfig(
      BillingResult result, BillingConfig billingConfig) {
    return new Messages.PlatformBillingConfigResponse.Builder()
        .setBillingResult(fromBillingResult(result))
        .setCountryCode(billingConfig.getCountryCode())
        .build();
  }

  /**
   * Converter from {@link BillingResult} and {@link AlternativeBillingOnlyReportingDetails} to map.
   */
  static Messages.PlatformAlternativeBillingOnlyReportingDetailsResponse
      fromAlternativeBillingOnlyReportingDetails(
          BillingResult result, AlternativeBillingOnlyReportingDetails details) {
    return new Messages.PlatformAlternativeBillingOnlyReportingDetailsResponse.Builder()
        .setBillingResult(fromBillingResult(result))
        .setExternalTransactionToken(details.getExternalTransactionToken())
        .build();
  }

  /**
   * Gets the symbol of for the given currency code for the default {@link Locale.Category#DISPLAY
   * DISPLAY} locale. For example, for the US Dollar, the symbol is "$" if the default locale is the
   * US, while for other locales it may be "US$". If no symbol can be determined, the ISO 4217
   * currency code is returned.
   *
   * @param currencyCode the ISO 4217 code of the currency
   * @return the symbol of this currency code for the default {@link Locale.Category#DISPLAY
   *     DISPLAY} locale
   * @exception NullPointerException if <code>currencyCode</code> is null
   * @exception IllegalArgumentException if <code>currencyCode</code> is not a supported ISO 4217
   *     code.
   */
  static String currencySymbolFromCode(String currencyCode) {
    return Currency.getInstance(currencyCode).getSymbol();
  }
}
