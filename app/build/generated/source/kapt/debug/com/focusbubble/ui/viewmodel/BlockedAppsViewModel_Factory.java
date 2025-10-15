package com.focusbubble.ui.viewmodel;

import com.focusbubble.data.repository.BlockedAppsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class BlockedAppsViewModel_Factory implements Factory<BlockedAppsViewModel> {
  private final Provider<BlockedAppsRepository> repositoryProvider;

  public BlockedAppsViewModel_Factory(Provider<BlockedAppsRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BlockedAppsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static BlockedAppsViewModel_Factory create(
      Provider<BlockedAppsRepository> repositoryProvider) {
    return new BlockedAppsViewModel_Factory(repositoryProvider);
  }

  public static BlockedAppsViewModel newInstance(BlockedAppsRepository repository) {
    return new BlockedAppsViewModel(repository);
  }
}
