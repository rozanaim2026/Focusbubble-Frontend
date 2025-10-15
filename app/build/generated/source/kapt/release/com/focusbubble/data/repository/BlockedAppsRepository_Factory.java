package com.focusbubble.data.repository;

import com.focusbubble.data.dao.BlockedAppDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class BlockedAppsRepository_Factory implements Factory<BlockedAppsRepository> {
  private final Provider<BlockedAppDao> daoProvider;

  public BlockedAppsRepository_Factory(Provider<BlockedAppDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public BlockedAppsRepository get() {
    return newInstance(daoProvider.get());
  }

  public static BlockedAppsRepository_Factory create(Provider<BlockedAppDao> daoProvider) {
    return new BlockedAppsRepository_Factory(daoProvider);
  }

  public static BlockedAppsRepository newInstance(BlockedAppDao dao) {
    return new BlockedAppsRepository(dao);
  }
}
