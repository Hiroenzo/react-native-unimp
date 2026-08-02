export {};

const mockSetOnUniMPEventCallBack = jest.fn();
const mockInvokeUniMPEventCallback = jest.fn().mockResolvedValue(true);
const mockNativeEventEmitter = jest.fn();

jest.mock('react-native', () => ({
  NativeEventEmitter: mockNativeEventEmitter,
  NativeModules: {
    Unimp: {
      setOnUniMPEventCallBack: mockSetOnUniMPEventCallBack,
      invokeUniMPEventCallback: mockInvokeUniMPEventCallback,
    },
  },
  // TurboModuleRegistry.get returns null in tests so that
  // NativeUniMP.ts falls back to NativeModules.Unimp (mocked above).
  TurboModuleRegistry: {
    get: jest.fn(() => null),
    getEnforcing: jest.fn(() => null),
  },
  Platform: {
    OS: 'ios',
    select: jest.fn(({ ios }) => ios),
  },
}));

const {
  UniMPEvent,
  UnimpEventEmitter,
}: typeof import('../index') = require('../index');
const {
  invokeUniMPEventCallback,
  setOnUniMPEventCallBack,
}: typeof import('../utils/Methods') = require('../utils/Methods');

describe('UniMP event callbacks', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('registers the native event callback', () => {
    setOnUniMPEventCallBack();

    expect(mockSetOnUniMPEventCallBack).toHaveBeenCalledTimes(1);
  });

  it('exports the native event emitter factory', () => {
    UnimpEventEmitter();

    expect(mockNativeEventEmitter).toHaveBeenCalledWith(
      expect.objectContaining({
        setOnUniMPEventCallBack: mockSetOnUniMPEventCallBack,
      })
    );
  });

  it('uses the native capsule close event name', () => {
    expect(UniMPEvent.onCapsuleCloseButtonClick).toBe(
      'onCapsuleCloseButtonClick'
    );
  });

  it.each([
    ['string response', 'received'],
    ['object response', { received: true }],
  ])('passes a %s to the native callback', async (_name, responseData) => {
    await expect(
      invokeUniMPEventCallback('callback-id', responseData)
    ).resolves.toBe(true);

    expect(mockInvokeUniMPEventCallback).toHaveBeenCalledWith(
      'callback-id',
      responseData
    );
  });
});

describe('TurboModule spec', () => {
  it('exports the Spec type from the codegen spec file', () => {
    // This test ensures the NativeUnimp.ts spec file is importable
    // and exports the expected shape for Codegen.
    const specModule = require('../NativeUnimp');
    expect(specModule).toBeDefined();
  });
});
