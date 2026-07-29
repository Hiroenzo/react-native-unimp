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
