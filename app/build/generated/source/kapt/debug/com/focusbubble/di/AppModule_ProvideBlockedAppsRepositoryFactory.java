package com.focusbubble.di;

import com.focusbubble.data.dao.BlockedAppDao;
import com.focusbubble.data.repository.BlockedAppsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideBlockedAppsRepositoryFactory implements Factory<BlockedAppsRepository> {
  private final Provider<BlockedAppDao> daoProvider;

  public AppModule_ProvideBlockedAppsRepositoryFactory(Provider<BlockedAppDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public BlockedAppsRepository get() {
    return provideBlockedAppsRepository(daoProvider.get());
  }

  public static AppModule_ProvideBlockedAppsRepositoryFactory create(
      Provider<BlockedAppDao> daoProvider) {
    return new AppModule_ProvideBlockedAppsRepositoryFactory(daoProvider);
  }

  public static BlockedAppsRepository provideBlockedAppsRepository(BlockedAppDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBlockedAppsRepository(dao));
  }
}
