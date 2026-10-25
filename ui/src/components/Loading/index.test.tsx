import { render } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import Loading from '.';
import { IntlWrapper } from 'components/utils/wrapper';

it('Loading snapshot', async () => {
  await (async () => {
    const { asFragment } = render(
      <Loading></Loading>
    , { wrapper: IntlWrapper});
    expect(asFragment()).toMatchSnapshot();
  })();
});
