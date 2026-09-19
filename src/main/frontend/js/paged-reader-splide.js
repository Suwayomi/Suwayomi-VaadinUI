import Splide from '@splidejs/splide';
import Panzoom from '@panzoom/panzoom';

window.initMangaSplide = function (carouselEl, dir, initialPageIndex,
    pageCount) {
  // Clean up any old splide instance on this element
  if (carouselEl._splide) {
    try {
      carouselEl._splide.destroy();
    } catch (e) {
      console.error("Error destroying old splide", e);
    }
  }

  const splide = new Splide(carouselEl, {
    type: 'slide',
    rewind: false,
    speed: 250, // Slightly faster transition speed for a snappier feel
    perPage: 1,
    arrows: false,
    pagination: false,
    direction: dir, // 'ltr' or 'rtl'
    drag: true,
    keyboard: false, // Managed by server/shortcuts to avoid input focus conflicts
    lazyLoad: 'nearby',
    preloadPages: 2, // Preload 2 pages ahead/behind to prevent loading delays during flipping
    start: initialPageIndex || 0,
    flickPower: 300, // Reduce flick momentum (default 600) to require a larger drag to switch pages
    dragMinThreshold: 40, // Require dragging at least 40px (default 10) before the gesture begins
  });

  // Setup panzoom on an image element
  function setupPanzoom(img) {
    if (!img || img._panzoom) {
      return;
    }

    const init = () => {
      if (img._panzoom) {
        return;
      } // Prevent double init

      const slide = img.closest('.splide__slide');
      if (!slide) {
        return;
      }

      const panzoom = Panzoom(img, {
        contain: null, // Start with no containment to prevent stretching/scaling-up at scale 1
        maxScale: 6,
        minScale: 1,
        panOnlyWhenZoomed: true,
        step: 0.45, // 1.5x larger zoom step size (default is 0.3)
        touchAction: '', // Set to empty to allow browser to handle swipes natively at scale 1
        handleStartEvent: (e) => {
          // If zoomed in, prevent default and stop propagation so panzoom handles panning.
          // If at normal scale (scale === 1), let events bubble so Splide can handle page swipes.
          if (img._panzoom && img._panzoom.getScale() > 1) {
            e.preventDefault();
            e.stopPropagation();
          }
        }
      });

      img._panzoom = panzoom;

      // Track dragging state for boundary-swiping page transition
      let startX = 0;
      let startPanX = 0;
      let isDraggingBoundary = false;
      let hasTriggered = false;

      img.addEventListener('pointerdown', (e) => {
        const scale = panzoom.getScale();
        if (scale <= 1) {
          isDraggingBoundary = false;
          return;
        }

        startX = e.clientX;
        startPanX = panzoom.getPan().x;
        isDraggingBoundary = true;
        hasTriggered = false;

        // Clear slide transition for snappier feedback during drag
        slide.style.transition = '';
      });

      img.addEventListener('pointermove', (e) => {
        if (!isDraggingBoundary || hasTriggered) {
          return;
        }
        const scale = panzoom.getScale();
        if (scale <= 1) {
          return;
        }

        const deltaX = e.clientX - startX;
        const contentWidth = img.clientWidth * scale;
        const containerWidth = img.parentElement.clientWidth;

        // Visual limit in screen pixels (allowing 75% of the page width to go off-screen)
        const maxTranslateX = Math.abs(contentWidth - containerWidth) / 2 + 0.75
            * contentWidth;

        // Convert startPanX (scaled coordinates) to screen coordinates for correct boundary check
        const startPanXScreen = startPanX * scale;

        const isAtLeftBoundary = (startPanXScreen >= maxTranslateX - 20);
        const isAtRightBoundary = (startPanXScreen <= -maxTranslateX + 20);

        let isOverpan = false;
        let directionFactor = 0;

        // 40px deadzone to prevent accidental swiping when exploring the page
        if (isAtLeftBoundary && deltaX > 40) {
          isOverpan = true;
          directionFactor = deltaX - 40;
        } else if (isAtRightBoundary && deltaX < -40) {
          isOverpan = true;
          directionFactor = deltaX + 40;
        } else if (contentWidth <= containerWidth && Math.abs(deltaX) > 40) {
          isOverpan = true;
          directionFactor = deltaX - Math.sign(deltaX) * 40;
        }

        if (isOverpan) {
          // Drag slide container slightly with rubber-banding resistance
          slide.style.transform = `translateX(${directionFactor * 0.3}px)`;
        } else {
          slide.style.transform = '';
        }
      });

      const handlePointerUp = (e) => {
        if (!isDraggingBoundary) {
          return;
        }
        isDraggingBoundary = false;

        const scale = panzoom.getScale();
        const deltaX = e.clientX - startX;

        // Snap the slide container back into place
        slide.style.transition = 'transform 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
        slide.style.transform = '';

        const threshold = 300; // Required large movement (300px) to switch pages
        if (!hasTriggered && Math.abs(deltaX) > threshold) {
          const contentWidth = img.clientWidth * scale;
          const containerWidth = img.parentElement.clientWidth;
          // Visual limit in screen pixels (allowing 50% of the page width to go off-screen)
          const maxTranslateX = Math.abs(contentWidth - containerWidth) / 2
              + 0.5 * contentWidth;

          const startPanXScreen = startPanX * scale;
          const isAtLeftBoundary = (startPanXScreen >= maxTranslateX - 20);
          const isAtRightBoundary = (startPanXScreen <= -maxTranslateX + 20);

          if (contentWidth <= containerWidth || (isAtLeftBoundary && deltaX > 0)
              || (isAtRightBoundary && deltaX < 0)) {
            hasTriggered = true;
            const isRtl = dir === 'rtl';
            if (deltaX < 0) {
              // Dragged left
              if (isRtl) {
                splide.go('<');
              } else {
                splide.go('>');
              }
            } else {
              // Dragged right
              if (isRtl) {
                splide.go('>');
              } else {
                splide.go('<');
              }
            }
          }
        }
      };

      img.addEventListener('pointerup', handlePointerUp);
      img.addEventListener('pointercancel', handlePointerUp);

      // Double click to toggle zoom on desktop
      img.parentElement.addEventListener('dblclick', (e) => {
        e.preventDefault();
        if (panzoom.getScale() > 1) {
          panzoom.reset();
        } else {
          panzoom.zoom(1.75, {animate: true}); // Zoom to 1.75x (half of 3.5x)
        }
      });

      // Double tap to toggle zoom on mobile
      let lastTap = 0;
      img.parentElement.addEventListener('touchend', (e) => {
        const currentTime = new Date().getTime();
        const tapLength = currentTime - lastTap;
        if (tapLength < 300 && tapLength > 0) {
          e.preventDefault();
          if (panzoom.getScale() > 1) {
            panzoom.reset();
          } else {
            panzoom.zoom(1.75, {animate: true});
          }
        }
        lastTap = currentTime;
      });

      // Wheel zoom support: zooms direct on mouse wheel events
      img.parentElement.addEventListener('wheel', (e) => {
        e.preventDefault();
        panzoom.zoomWithWheel(e);
      }, {passive: false});

      // Handle custom containment constraints and Splide drag toggling on panzoomchange
      img.addEventListener('panzoomchange', (e) => {
        const {x, y, scale} = e.detail;

        if (scale > 1) {
          splide.options = {drag: false};
          panzoom.setOptions({touchAction: 'none'}); // Lock touch action for panning

          // Clamp pan coordinates manually
          const contentWidth = img.clientWidth * scale;
          const containerWidth = img.parentElement.clientWidth;
          const contentHeight = img.clientHeight * scale;
          const containerHeight = img.parentElement.clientHeight;

          // Unified containment limit in scaled coordinates:
          // If the scaled dimension is larger than the container, prevent exposing background.
          // If the scaled dimension is smaller than the container, keep the image inside the container boundaries.
          // Clamp boundaries horizontally allowing 50% of the page width to go off-screen,
          // and vertically allowing 20% of the page height to go off-screen:
          const maxTranslateX = Math.abs(contentWidth - containerWidth) / (2
              * scale) + 0.8 * img.clientWidth;
          const maxTranslateY = Math.abs(contentHeight - containerHeight) / (2
              * scale) + 0.2 * img.clientHeight;

          const clampedX = Math.max(-maxTranslateX, Math.min(maxTranslateX, x));
          const clampedY = Math.max(-maxTranslateY, Math.min(maxTranslateY, y));

          if (clampedX !== x || clampedY !== y) {
            panzoom.pan(clampedX, clampedY, {silent: true});
          }
        } else {
          splide.options = {drag: true};
          panzoom.setOptions({touchAction: ''}); // Restore empty touch action to allow swipes natively

          // Reset alignment to center when scale is 1.0
          if (x !== 0 || y !== 0) {
            panzoom.pan(0, 0, {silent: true});
          }
        }
      });
    };

    if (img.complete || img.naturalWidth > 0) {
      init();
    } else {
      img.addEventListener('load', init);
    }
  }

  // Find and initialize panzoom on existing manga-pages
  const setupAllExisting = () => {
    const pages = carouselEl.querySelectorAll('.manga-page');
    pages.forEach(setupPanzoom);
  };

  splide.on('mounted', setupAllExisting);
  splide.on('lazyloaded', setupPanzoom);

  splide.on('moved', (newIndex) => {
    // Reset zoom on all other pages when slide changes
    const pages = carouselEl.querySelectorAll('.manga-page');
    pages.forEach((img, idx) => {
      if (idx !== newIndex && img._panzoom) {
        img._panzoom.reset({animate: false});
      }
    });
  });

  splide.mount();
  carouselEl._splide = splide;

  // Initialize for the first loaded slide immediately since mount already ran or setup is async
  setupAllExisting();

  // Handle page views immediately when the slide transition starts
  splide.on('move', (newIndex) => {
    // Send event to server
    const event = new CustomEvent('manga-page-view', {
      detail: {pageIndex: newIndex},
      bubbles: true
    });
    carouselEl.dispatchEvent(event);

    // Update UI controls immediately on the client side for instant feedback
    const readerContainer = carouselEl.closest('.manga-reader');
    if (readerContainer) {
      const controls = readerContainer.querySelector('.controls');
      if (controls) {
        // Update the progress bar CSS variable
        if (pageCount && pageCount > 0) {
          const progress = ((newIndex + 1) / pageCount) * 100;
          controls.style.setProperty('--reader-progress', progress + '%');
        }
        // Update the text field input value
        const input = controls.querySelector('.page-input');
        if (input) {
          input.value = String(newIndex + 1);
        }
      }
    }
  });
};
